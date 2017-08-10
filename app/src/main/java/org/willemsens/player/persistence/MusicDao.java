package org.willemsens.player.persistence;

import android.util.Log;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;

import java.util.List;
import java.util.Set;

public class MusicDao {
    private final EntityDataStore<Persistable> dataStore;

    public MusicDao(EntityDataStore<Persistable> dataStore) {
        this.dataStore = dataStore;
    }

    public List<Directory> getAllDirectories() {
        return this.dataStore.select(Directory.class).get().toList();
    }

    public List<Album> getAllAlbums() {
        return this.dataStore.select(Album.class).get().toList();
    }

    public List<Artist> getAllArtists() {
        return this.dataStore.select(Artist.class).get().toList();
    }

    public List<Song> getAllSongs() {
        return this.dataStore.select(Song.class).get().toList();
    }

    /**
     * Checks albums in the DB. If an album exists, its ID is updated in the POJO. If the album doesn't
     * exist, it's inserted in the DB and the new ID is set in the POJO.
     * @param albums The set of albums to check for in the DB.
     */
    public void checkAlbumsSelectInsert(Set<Album> albums, Set<Song> songs) {
        final List<Album> databaseAlbums = getAllAlbums();
        for (Album album : albums) {
            if (databaseAlbums.contains(album)) {
                for (Album databaseAlbum : databaseAlbums) {
                    if (album.equals(databaseAlbum)) {
                        // FIXME album.setId(databaseAlbum.getId());
                        break;
                    }
                }
            } else {
                // TODO: calculate total length for all songs in 'album'
                //       simply iterate all songs and
                insertAlbum(album);
            }
        }
    }

    /**
     * Checks artists in the DB. If an artist exists, its ID is updated in the POJO. If the artist doesn't
     * exist, it's inserted in the DB and the new ID is set in the POJO.
     * @param artists The set of artists to check for in the DB.
     */
    public void checkArtistsSelectInsert(Set<Artist> artists, Set<Album> albums, Set<Song> songs) {
        final List<Artist> databaseArtists = getAllArtists();
        for (Artist artist : artists) {
            if (databaseArtists.contains(artist)) {
                for (Artist databaseArtist : databaseArtists) {
                    if (artist.equals(databaseArtist)) {
                        // FIXME artist.setId(databaseArtist.getId());
                        break;
                    }
                }
            } else {
                insertArtist(artist);
            }
        }
    }

    public void checkSongsSelectInsert(Set<Song> songs) {
        List<Song> dbSongs;
        for (Song song : songs) {
            dbSongs = this.dataStore.select(Song.class).where(Song.FILE.equal(song.getFile())).get().toList();
            if (dbSongs.size() == 0) {
                insertSong(song);
            }
        }
    }

    private void insertAlbum(Album album) {
        this.dataStore.insert(album);
        Log.d(getClass().getName(), "Inserted Album: " + album);
    }

    private void insertArtist(Artist artist) {
        this.dataStore.insert(artist);
        Log.d(getClass().getName(), "Inserted Artist: " + artist);
    }

    private void insertSong(Song song) {
        this.dataStore.insert(song);
        Log.d(getClass().getName(), "Inserted Song: " + song);
    }
}
