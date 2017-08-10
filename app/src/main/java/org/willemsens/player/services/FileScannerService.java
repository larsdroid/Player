package org.willemsens.player.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.PlayerApplication;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
    private EntityDataStore<Persistable> dataStore;
    private MusicDao musicDao;

    public FileScannerService() {
        super(FileScannerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (this.dataStore == null) {
            this.dataStore = ((PlayerApplication)getApplication()).getData();
        }
        if (this.musicDao == null) {
            this.musicDao = new MusicDao(this.dataStore);
        }

        Set<Song> songs = new HashSet<>();
        Set<Album> albums = new HashSet<>();
        Set<Artist> artists = new HashSet<>();

        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.getPath()).getCanonicalFile();
                if (root.isDirectory()) {
                    processDirectory(root, songs, albums, artists);
                    this.musicDao.checkArtistsSelectInsert(artists, albums, songs);
                    this.musicDao.checkAlbumsSelectInsert(albums, songs);
                    this.musicDao.checkSongsSelectInsert(songs);

                    // TODO: The above three method calls should return how many records were inserted.
                    //       In case new records were inserted of a given type, a broadcast should be sent using
                    //       LocalBroadcastManager. Fragments (or adapters should refresh their content when
                    //       a broadcast is received.
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
