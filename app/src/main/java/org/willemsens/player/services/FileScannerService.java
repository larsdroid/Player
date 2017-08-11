package org.willemsens.player.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
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
                    int artistInserts = this.musicDao.checkArtistsSelectInsert(artists, albums, songs);
                    int albumInserts = this.musicDao.checkAlbumsSelectInsert(albums, songs);
                    int songInserts = this.musicDao.checkSongsSelectInsert(songs);

                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
                    Intent broadcast;

                    if (artistInserts > 0) {
                        broadcast = new Intent(getString(R.string.key_artists_inserted));
                        lbm.sendBroadcast(broadcast);
                    }

                    if (albumInserts > 0) {
                        broadcast = new Intent(getString(R.string.key_albums_inserted));
                        lbm.sendBroadcast(broadcast);
                    }

                    if (songInserts > 0) {
                        broadcast = new Intent(getString(R.string.key_songs_inserted));
                        lbm.sendBroadcast(broadcast);
                    }
                } else {
                    Log.e(getClass().getName(), root.getAbsolutePath() + " is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
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
                } else {
                    song = AudioFileReader.readSong(canonicalFile);
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
