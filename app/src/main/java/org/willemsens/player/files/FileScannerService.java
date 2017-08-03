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
import java.util.Iterator;
import java.util.Set;

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
            this.musicDao = new MusicDao(getApplicationContext());
        }

        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.getPath()).getCanonicalFile();
                if (root.isDirectory()) {
                    processDirectory(root);
                } else {
                    Log.e(getClass().getName(), root.getAbsolutePath() + " is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processDirectory(File currentRoot) throws IOException {
        final Set<Song> songs = new HashSet<>();
        final Set<Album> albums = new HashSet<>();
        final Set<Artist> artists = new HashSet<>();

        final File[] files = currentRoot.listFiles();
        if (files != null) {
            Song song;

            for (File file : files) {
                final File canonicalFile = file.getCanonicalFile();
                if (canonicalFile.isDirectory()) {
                    processDirectory(canonicalFile);
                } else {
                    song = AudioFileReader.readSong(canonicalFile);
                    songs.add(song);
                    albums.add(song.getAlbum());
                    artists.add(song.getArtist());
                    artists.add(song.getAlbum().getArtist());
                }
            }
        }

        // TODO: filter songs here

        // TODO: add songs here

        removeExistingAlbums(albums);

        // TODO: calculate total length for each album in 'albums'
        //       simply iterate all songs and sum those with an equal album (Album::equals)

        // TODO: add each album in 'albums'

        // TODO: filter artists here

        // TODO: add artists here
    }

    private void removeExistingAlbums(Set<Album> albums) {
        for (Iterator<Album> it = albums.iterator(); it.hasNext(); ) {
            Album album =  it.next();
            if (this.musicDao.albumExists(album)) {
                it.remove();
            }
        }
    }
}
