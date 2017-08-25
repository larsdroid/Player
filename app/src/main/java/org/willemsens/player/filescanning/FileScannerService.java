package org.willemsens.player.filescanning;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.fetchers.AlbumInfoFetcherService;
import org.willemsens.player.fetchers.ArtistInfoFetcherService;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

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
            final EntityDataStore<Persistable> dataStore = ((PlayerApplication)getApplication()).getData();
            this.musicDao = new MusicDao(dataStore);
        }

        Set<Song> songs = new HashSet<>();
        Set<Album> albums = new HashSet<>();
        Set<Artist> artists = new HashSet<>();

        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.getPath()).getCanonicalFile();
                if (root.isDirectory()) {
                    processDirectory(root, songs, albums, artists);
                    final List<Long> artistIds = this.musicDao.checkArtistsSelectInsert(artists, albums, songs);
                    final List<Long> albumIds = this.musicDao.checkAlbumsSelectInsert(albums, songs);
                    final List<Long> songIds = this.musicDao.checkSongsSelectInsert(songs);

                    newRecordsInserted(
                            artistIds,
                            getString(R.string.key_artist_id),
                            getString(R.string.key_artist_inserted),
                            getString(R.string.key_artists_inserted),
                            ArtistInfoFetcherService.class);

                    newRecordsInserted(
                            albumIds,
                            getString(R.string.key_album_id),
                            getString(R.string.key_album_inserted),
                            getString(R.string.key_albums_inserted),
                            AlbumInfoFetcherService.class);

                    newRecordsInserted(
                            songIds,
                            getString(R.string.key_song_id),
                            getString(R.string.key_song_inserted),
                            getString(R.string.key_songs_inserted),
                            null);
                } else {
                    Log.e(getClass().getName(), root.getAbsolutePath() + " is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call this method after new records have been inserted into the DB. This method will send
     * broadcasts to refresh other parts of the application. Either a single "full refresh"
     * broadcast in case the amount of new records reached a certain threshold, or a few "record
     * refresh" broadcasts in case the amount of new records didn't reach the threshold.
     * If a service class is provided as well, then the service in question is launched. Either for
     * a full refresh or for multiple single record updates, similar to the broadcast.
     * @param recordIds The IDs of the new records.
     * @param intentKeyRecordId The intent key to use for submitting a single ID with an intent.
     * @param intentActionSingleRecord The intent action to use when broadcasting for a single
     *                                 record insert.
     * @param intentActionMultipleRecords The intent action to use when broadcasting for a full
     *                                    refresh.
     * @param clazz The service to trigger.
     */
    private void newRecordsInserted(@NonNull List<Long> recordIds,
                                    @NonNull String intentKeyRecordId,
                                    @NonNull String intentActionSingleRecord,
                                    @NonNull String intentActionMultipleRecords,
                                    @Nullable Class clazz) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent broadcast;

        if (recordIds.size() > THRESHOLD_FULL_REFRESH) {
            broadcast = new Intent(intentActionMultipleRecords);
            lbm.sendBroadcast(broadcast);

            if (clazz != null) {
                broadcast = new Intent(this, clazz);
                startService(broadcast);
            }
        } else if (recordIds.size() > 0) {
            for (Long recordId : recordIds) {
                broadcast = new Intent(intentActionSingleRecord);
                broadcast.putExtra(intentKeyRecordId, recordId);
                lbm.sendBroadcast(broadcast);

                if (clazz != null) {
                    broadcast = new Intent(this, clazz);
                    broadcast.putExtra(intentKeyRecordId, recordId);
                    startService(broadcast);
                }
            }
        }
    }

    private void processDirectory(File currentRoot, Set<Song> songs, Set<Album> albums, Set<Artist> artists) throws IOException {
        final File[] files = currentRoot.listFiles();
        if (files != null) {
            Song song;

            for (File file : files) {
                final File canonicalFile = file.getCanonicalFile();
                if (canonicalFile.isDirectory()) {
                    processDirectory(canonicalFile, songs, albums, artists);
                } else if (isMusicFile(canonicalFile)) {
                    song = AudioFileReader.readSong(canonicalFile);
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
