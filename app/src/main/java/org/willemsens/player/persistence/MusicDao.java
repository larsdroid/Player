package org.willemsens.player.persistence;

import android.util.Log;

import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;

import java.util.List;
import java.util.Set;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

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
     * Checks albums in the DB. If an album exists, all songs with this album are updated to have
     * the corresponding album from the DB. If the album doesn't exist, it is inserted into the DB.
     * @param albums The set of albums to check for in the DB.
     * @param songs The songs that may have to be updated in case their album turns out
     *              to be in the DB already.
     */
    public void checkAlbumsSelectInsert(Set<Album> albums, Set<Song> songs) {
        final List<Album> databaseAlbums = getAllAlbums();
        for (Album album : albums) {
            if (databaseAlbums.contains(album)) {
                for (Album databaseAlbum : databaseAlbums) {
                    if (album.equals(databaseAlbum)) {
                        for (Song song : songs) {
                            if (song.getAlbum().equals(album)) {
                                song.setAlbum(databaseAlbum);
                            }
                        }
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
     * Checks artists in the DB. If an artist exists, all songs and albums with this artist are
     * updated to have the corresponding artist from the DB. If the artist doesn't exist, it is
     * inserted into the DB.
     * @param artists The set of artists to check for in the DB.
     * @param albums The albums that may have to be updated in case their artist turns out
     *               to be in the DB already.
     * @param songs The songs that may have to be updated in case their artist turns out
     *              to be in the DB already.
     */
    public void checkArtistsSelectInsert(Set<Artist> artists, Set<Album> albums, Set<Song> songs) {
        final List<Artist> databaseArtists = getAllArtists();
        for (Artist artist : artists) {
            if (databaseArtists.contains(artist)) {
                for (Artist databaseArtist : databaseArtists) {
                    if (artist.equals(databaseArtist)) {
                        for (Album album : albums) {
                            if (album.getArtist().equals(artist)) {
                                album.setArtist(databaseArtist);
                            }
                        }
                        for (Song song : songs) {
                            if (song.getArtist().equals(artist)) {
                                song.setArtist(databaseArtist);
                            }
                        }
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
