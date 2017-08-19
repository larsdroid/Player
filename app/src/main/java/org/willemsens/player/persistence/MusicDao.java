package org.willemsens.player.persistence;

import android.os.Environment;
import android.util.Log;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    public List<Album> getAllAlbumsMissingInfo() {
        return this.dataStore.select(Album.class)
                .where(Album.IMAGE.isNull().or(Album.YEAR_RELEASED.isNull()))
                .get().toList();
    }

    public List<Artist> getAllArtistsMissingImage() {
        return this.dataStore.select(Artist.class)
                .where(Artist.IMAGE.isNull())
                .get().toList();
    }

    public void saveImage(Image image) {
        this.dataStore.insert(image);
        Log.v(getClass().getName(), "Inserted Image: " + image);
    }

    public void updateAlbum(Album album) {
        this.dataStore.update(album);
        Log.v(getClass().getName(), "Updated Album: " + album);
    }

    public void updateArtist(Artist artist) {
        this.dataStore.update(artist);
        Log.v(getClass().getName(), "Updated Artist: " + artist);
    }

    public Album findAlbum(long id) {
        return this.dataStore.select(Album.class)
                .where(Album.ID.equal(id))
                .get().firstOrNull();
    }

    public Artist findArtist(long id) {
        return this.dataStore.select(Artist.class)
                .where(Artist.ID.equal(id))
                .get().firstOrNull();
    }

    public Song findSong(long id) {
        return this.dataStore.select(Song.class)
                .where(Song.ID.equal(id))
                .get().firstOrNull();
    }

    /**
     * Checks albums in the DB. If an album exists, all songs with this album are updated to have
     * the corresponding album from the DB. If the album doesn't exist, it is inserted into the DB.
     * @param albums The set of albums to check for in the DB.
     * @param songs The songs that may have to be updated in case their album turns out
     *              to be in the DB already.
     * @return The amount of new albums inserted into the DB.
     */
    public List<Long> checkAlbumsSelectInsert(Set<Album> albums, Set<Song> songs) {
        final List<Album> databaseAlbums = getAllAlbums();
        final List<Long> insertedIds = new ArrayList<>();
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
                insertedIds.add(album.getId());
            }
        }
        return insertedIds;
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
     * @return The amount of new artists inserted into the DB.
     */
    public List<Long> checkArtistsSelectInsert(Set<Artist> artists, Set<Album> albums, Set<Song> songs) {
        final List<Artist> databaseArtists = getAllArtists();
        final List<Long> insertedIds = new ArrayList<>();
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
                insertedIds.add(artist.getId());
            }
        }
        return insertedIds;
    }

    public List<Long> checkSongsSelectInsert(Set<Song> songs) {
        List<Song> dbSongs;
        final List<Long> insertedIds = new ArrayList<>();
        for (Song song : songs) {
            dbSongs = this.dataStore.select(Song.class).where(Song.FILE.equal(song.getFile())).get().toList();
            if (dbSongs.size() == 0) {
                insertSong(song);
                insertedIds.add(song.getId());
            }
        }
        return insertedIds;
    }

    private void insertAlbum(Album album) {
        this.dataStore.insert(album);
        Log.v(getClass().getName(), "Inserted Album: " + album);
    }

    private void insertArtist(Artist artist) {
        this.dataStore.insert(artist);
        Log.v(getClass().getName(), "Inserted Artist: " + artist);
    }

    private void insertSong(Song song) {
        this.dataStore.insert(song);
        Log.v(getClass().getName(), "Inserted Song: " + song);
    }

    /**
     * This helper method should only be called once, right after the app has been installed for
     * the first time. Should be called after READ_EXTERNAL_STORAGE has been granted and before
     * the FileScannerService is launched for the first time.
     */
    public void afterInstallationSetup() {
        checkInsertMusicPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        if (System.getenv("SECONDARY_STORAGE") != null) {
            checkInsertMusicPath(System.getenv("SECONDARY_STORAGE") + "/" + Environment.DIRECTORY_MUSIC);
        }
    }

    /**
     * Checks if a path is an existing directory and if so inserts it into the DB as default music
     * directory.
     * @param path The path to check.
     * @return 'true' in case the path was inserted into the DB. 'false' otherwise.
     */
    private boolean checkInsertMusicPath(String path) {
        return checkInsertMusicPath(new File(path));
    }

    /**
     * Checks if a path is an existing directory and if so inserts it into the DB as default music
     * directory.
     * @param path The path to check.
     * @return 'true' in case the path was inserted into the DB. 'false' otherwise.
     */
    private boolean checkInsertMusicPath(File path) {
        try {
            final File canonicalPath = path.getCanonicalFile();
            if (canonicalPath.isDirectory() && canonicalPath.listFiles() != null) {
                insertMusicPath(canonicalPath.getCanonicalPath());
                return true;
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error while checking path: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserts the given path as a default music directory into the DB.
     * @param path The path to insert into the DB.
     */
    private void insertMusicPath(String path) {
        final Directory directory = new Directory();
        directory.setPath(path);
        this.dataStore.insert(directory);
    }
}
