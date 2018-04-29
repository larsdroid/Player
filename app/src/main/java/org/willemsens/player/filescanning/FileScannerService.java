package org.willemsens.player.filescanning;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.PlayerApplication;
import org.willemsens.player.fetchers.AlbumInfoFetcherService;
import org.willemsens.player.fetchers.ArtistInfoFetcherService;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastBuilder;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastType;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_SONG_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUMS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTIST_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_SONGS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_SONG_INSERTED;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
    private static final int THRESHOLD_FULL_REFRESH = 10;

    private static final String[] SUPPORTED_FORMATS = {"flac", "mkv", "mp3", "ogg", "wav"};

    private MusicDao musicDao;

    public FileScannerService() {
        super(FileScannerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (this.musicDao == null) {
            final EntityDataStore<Persistable> dataStore = ((PlayerApplication) getApplication()).getData();
            this.musicDao = new MusicDao(dataStore, this);
        }

        scanMediaStoreFiles();
        scanCustomDirectories();
    }

    private void scanMediaStoreFiles(Uri musicUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        String[] selectionArgs = {"%/audio/ui/%"};
        ContentResolver musicResolver = getContentResolver();
        Cursor musicCursor = musicResolver.query(musicUri, proj,
                MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ?",
                selectionArgs, null);

        if (musicCursor != null) {
            if (musicCursor.moveToFirst()) {
                final int fileColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                Set<Song> songs = new HashSet<>();
                Set<Album> albums = new HashSet<>();
                Set<Artist> artists = new HashSet<>();

                do {
                    Log.d("FileScannerService", musicCursor.getString(fileColumn));

                    final File file = new File(musicCursor.getString(fileColumn));
                    try {
                        final File canonicalFile = file.getCanonicalFile();
                        processSingleFile(canonicalFile, songs, albums, artists);
                    } catch (IOException e) {
                        Log.e("FileScannerService", e.getMessage());
                    }
                } while (musicCursor.moveToNext());

                insertRecords(songs, albums, artists);
            }
            musicCursor.close();
        }
    }

    private void scanMediaStoreFiles() {
        scanMediaStoreFiles(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        scanMediaStoreFiles(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    private void scanCustomDirectories() {
        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.getPath()).getCanonicalFile();
                if (root.isDirectory()) {
                    Set<Song> songs = new HashSet<>();
                    Set<Album> albums = new HashSet<>();
                    Set<Artist> artists = new HashSet<>();

                    processDirectory(root, songs, albums, artists);

                    insertRecords(songs, albums, artists);
                } else {
                    Log.e(getClass().getName(), root.getAbsolutePath() + " is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertRecords(Set<Song> songs, Set<Album> albums, Set<Artist> artists) {
        final List<Long> artistIds = this.musicDao.insertArtistsIfNotExist(artists);
        final List<Long> albumIds = this.musicDao.insertAlbumsIfNotExist(albums);
        final List<Long> songIds = this.musicDao.insertSongsIfNotExist(songs);

        newRecordsInserted(
                artistIds,
                MLBPT_ARTIST_ID,
                MLBT_ARTIST_INSERTED,
                MLBT_ARTISTS_INSERTED,
                ArtistInfoFetcherService.class);

        newRecordsInserted(
                albumIds,
                MLBPT_ALBUM_ID,
                MLBT_ALBUM_INSERTED,
                MLBT_ALBUMS_INSERTED,
                AlbumInfoFetcherService.class);

        newRecordsInserted(
                songIds,
                MLBPT_SONG_ID,
                MLBT_SONG_INSERTED,
                MLBT_SONGS_INSERTED,
                null);
    }

    /**
     * Call this method after new records have been inserted into the DB. This method will send
     * broadcasts to refresh other parts of the application. Either a single "full refresh"
     * broadcast in case the amount of new records reached a certain threshold, or a few "record
     * refresh" broadcasts in case the amount of new records didn't reach the threshold.
     * If a service class is provided as well, then the service in question is launched. Either for
     * a full refresh or for multiple single record updates, similar to the broadcast.
     *
     * @param recordIds                    The IDs of the new records.
     * @param payloadType                  The intent extra payload key to use for submitting a single ID with an intent.
     * @param broadcastTypeSingleRecord    The broadcast type to use when broadcasting for a single
     *                                     record insert.
     * @param broadcastTypeMultipleRecords The broadcast type to use when broadcasting for a full
     *                                     refresh.
     * @param clazz                        The service to trigger.
     */
    private void newRecordsInserted(@NonNull List<Long> recordIds,
                                    @NonNull MusicLibraryBroadcastPayloadType payloadType,
                                    @NonNull MusicLibraryBroadcastType broadcastTypeSingleRecord,
                                    @NonNull MusicLibraryBroadcastType broadcastTypeMultipleRecords,
                                    @Nullable Class clazz) {
        MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this);

        if (recordIds.size() > THRESHOLD_FULL_REFRESH) {
            builder
                    .setType(broadcastTypeMultipleRecords)
                    .buildAndSubmitBroadcast();

            if (clazz != null) {
                builder
                        .setClass(clazz)
                        .buildAndSubmitService();
            }
        } else if (recordIds.size() > 0) {
            for (Long recordId : recordIds) {
                builder
                        .setType(broadcastTypeSingleRecord)
                        .setRecordId(payloadType, recordId)
                        .buildAndSubmitBroadcast();

                if (clazz != null) {
                    builder
                            .setType(broadcastTypeSingleRecord)
                            .setClass(clazz)
                            .setRecordId(payloadType, recordId)
                            .buildAndSubmitService();
                }
            }
        }
    }

    private void processDirectory(File currentRoot, Set<Song> songs, Set<Album> albums, Set<Artist> artists) throws IOException {
        final File[] files = currentRoot.listFiles();
        if (files != null) {
            for (File file : files) {
                final File canonicalFile = file.getCanonicalFile();
                if (canonicalFile.isDirectory()) {
                    processDirectory(canonicalFile, songs, albums, artists);
                } else {
                    processSingleFile(canonicalFile, songs, albums, artists);
                }
            }
        }
    }

    private void processSingleFile(File canonicalFile, Set<Song> songs, Set<Album> albums, Set<Artist> artists) {
        if (isMusicFile(canonicalFile)) {
            Song song = AudioFileReader.readSong(canonicalFile);
            if (song != null) {
                songs.add(song);

                if (artists.contains(song.getArtist())) {
                    for (Artist artist : artists) {
                        if (artist.equals(song.getArtist())) {
                            song.setArtist(artist);
                            break;
                        }
                    }
                } else {
                    artists.add(song.getArtist());
                }

                if (albums.contains(song.getAlbum())) {
                    for (Album album : albums) {
                        if (album.equals(song.getAlbum())) {
                            song.setAlbum(album);
                            break;
                        }
                    }
                } else {
                    if (artists.contains(song.getAlbum().getArtist())) {
                        for (Artist artist : artists) {
                            if (artist.equals(song.getAlbum().getArtist())) {
                                song.getAlbum().setArtist(artist);
                                break;
                            }
                        }
                    } else {
                        artists.add(song.getAlbum().getArtist());
                    }
                    albums.add(song.getAlbum());
                }
            }
        }
    }

    private boolean isMusicFile(File canonicalFile) {
        if (canonicalFile.isFile()) {
            final String fileName = canonicalFile.getName();
            final int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex + 1 < fileName.length()) {
                final String extension = fileName.substring(dotIndex + 1, fileName.length());
                for (String supportedExtension : SUPPORTED_FORMATS) {
                    if (supportedExtension.equalsIgnoreCase(extension)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
