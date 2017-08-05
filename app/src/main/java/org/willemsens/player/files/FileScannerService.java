package org.willemsens.player.files;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
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
    private MusicDao musicDao;

    private Set<Song> songs;
    private Set<Album> albums;
    private Set<Artist> artists;

    public FileScannerService() {
        super(FileScannerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (this.musicDao == null) {
            this.musicDao = new MusicDao(getApplicationContext());
        }

        this.songs = new HashSet<>();
        this.albums = new HashSet<>();
        this.artists = new HashSet<>();

        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.getPath()).getCanonicalFile();
                if (root.isDirectory()) {
                    processDirectory(root);

                    this.musicDao.removeExistingArtists(artists);
                    this.musicDao.insertArtists(artists);

                    this.musicDao.removeExistingAlbums(albums);
                    // TODO: calculate total length for each album in 'albums'
                    //       simply iterate all songs and sum those with an equal album (Album::equals)
                    this.musicDao.insertAlbums(albums);

                    // TODO: filter songs here
                    // TODO: add songs here
                } else {
                    Log.e(getClass().getName(), root.getAbsolutePath() + " is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processDirectory(File currentRoot) throws IOException {
        final File[] files = currentRoot.listFiles();
        if (files != null) {
            Song song;

            for (File file : files) {
                final File canonicalFile = file.getCanonicalFile();
                if (canonicalFile.isDirectory()) {
                    processDirectory(canonicalFile);
                } else {
                    song = AudioFileReader.readSong(canonicalFile);
                    this.songs.add(song);

                    if (this.artists.contains(song.getArtist())) {
                        for (Artist artist : this.artists) {
                            if (artist.equals(song.getArtist())) {
                                song.setArtist(artist);
                                break;
                            }
                        }
                    } else {
                        this.artists.add(song.getArtist());
                    }

                    if (this.albums.contains(song.getAlbum())) {
                        for (Album album : this.albums) {
                            if (album.equals(song.getAlbum())) {
                                song.setAlbum(album);
                                break;
                            }
                        }
                    } else {
                        if (this.artists.contains(song.getAlbum().getArtist())) {
                            for (Artist artist : this.artists) {
                                if (artist.equals(song.getAlbum().getArtist())) {
                                    song.getAlbum().setArtist(artist);
                                    break;
                                }
                            }
                        } else {
                            this.artists.add(song.getAlbum().getArtist());
                        }
                        this.albums.add(song.getAlbum());
                    }
                }
            }
        }
    }
}
