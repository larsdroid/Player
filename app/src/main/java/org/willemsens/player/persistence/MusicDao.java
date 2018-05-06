package org.willemsens.player.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.ApplicationState;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.Song;
import org.willemsens.player.playback.PlayMode;
import org.willemsens.player.playback.PlayStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_MILLIS;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_PLAY_MODE;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_PLAY_STATUS;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_SONG_ID;

@Dao
public abstract class MusicDao {
    @Query("SELECT * FROM directory")
    public abstract List<Directory> getAllDirectories();

    @Query("SELECT * FROM album")
    public abstract List<Album> getAllAlbums();

    @Query("SELECT * FROM artist")
    public abstract List<Artist> getAllArtists();

    @Query("SELECT * FROM song")
    public abstract List<Song> getAllSongs();

    @Query("SELECT * FROM album WHERE imageId IS NULL OR yearReleased IS NULL")
    public abstract List<Album> getAllAlbumsMissingInfo();

    @Query("SELECT * FROM artist WHERE imageId IS NULL")
    public abstract List<Artist> getAllArtistsMissingImage();

    @Query("SELECT * FROM album WHERE id = :id")
    public abstract Album findAlbum(long id);

    @Query("SELECT * FROM artist WHERE id = :id")
    public abstract Artist findArtist(long id);

    @Query("SELECT * FROM artist WHERE name = :name")
    abstract Artist findArtist(@NonNull String name);

    @Query("SELECT * FROM album WHERE name = :name AND artistId = :artistId")
    abstract Album findAlbum(@NonNull String name, int artistId);

    @Query("SELECT * FROM song WHERE file = :file")
    abstract Song findSong(@NonNull String file);

    @Query("SELECT * FROM song WHERE id = :id")
    public abstract Song findSong(long id);

    @Query("SELECT * FROM song WHERE albumId = :albumId AND track > :previousTrack ORDER BY track ASC LIMIT 1")
    public abstract Song findNextSong(int albumId, int previousTrack);

    @Query("SELECT * FROM song WHERE albumId = :albumId AND track < :followingTrack ORDER BY track DESC LIMIT 1")
    public abstract Song findPreviousSong(int albumId, int followingTrack);

    @Query("SELECT * FROM song WHERE albumId = :albumId ORDER BY track ASC")
    public abstract List<Song> getAllSongs(int albumId);

    @Query("SELECT * FROM song WHERE albumId = :albumId ORDER BY track ASC LIMIT 1")
    public abstract Song findFirstSong(int albumId);

    @Query("SELECT * FROM song WHERE albumId = :albumId ORDER BY track DESC LIMIT 1")
    public abstract Song findLastSong(int albumId);

    @Query("SELECT * FROM applicationstate WHERE property = :property LIMIT 1")
    abstract ApplicationState getApplicationState(String property);

    @Update
    public abstract void updateAlbum(Album album);

    @Update
    public abstract void updateArtist(Artist artist);

    @Update
    abstract void updateApplicationState(ApplicationState applicationState);

    @Insert
    abstract void insertAlbum(Album album);

    @Insert
    abstract void insertArtist(Artist artist);

    @Insert
    abstract void insertSong(Song song);

    @Insert
    abstract void insertDirectory(Directory directory);

    @Insert
    abstract void insertApplicationState(ApplicationState applicationState);

    @Insert
    public abstract int insertImage(Image image);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract List<Long> insertArtistsIfNotExist(Set<Artist> artists);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract List<Long> insertAlbumsIfNotExist(Set<Album> albums);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract List<Long> insertSongsIfNotExist(Set<Song> songs);

    @Delete
    public abstract void deleteDirectory(Directory directory);

    @Query("DELETE FROM directory")
    public abstract void deleteAllDirectories();

    @Query("DELETE FROM artist")
    abstract void deleteAllArtists();

    @Query("DELETE FROM album")
    abstract void deleteAllAlbums();

    @Query("DELETE FROM image")
    abstract void deleteAllImages();

    @Query("DELETE FROM song")
    abstract void deleteAllSongs();

    @Query("DELETE FROM applicationstate WHERE property = :property")
    public abstract void deleteApplicationState(String property);

    public Artist findOrCreateArtist(String artistName, Consumer<Artist> handleInsertedArtist) {
        Artist artist = findArtist(artistName);
        if (artist == null) {
            artist = new Artist(artistName);
            insertArtist(artist);
            Observable.just(artist).subscribe(handleInsertedArtist).dispose();
        }
        return artist;
    }

    public Album findOrCreateAlbum(String albumName, int artistId, Integer albumYear, Consumer<Album> handleInsertedAlbum) {
        Album album = findAlbum(albumName, artistId);
        if (album == null) {
            album = new Album(albumName, artistId);
            album.yearReleased = albumYear;
            insertAlbum(album);
            Observable.just(album).subscribe(handleInsertedAlbum).dispose();
        }
        return album;
    }

    public Song findOrCreateSong(@NonNull String songName, int songArtistId, int albumId, int track, @NonNull String file,
                                 int songLength, Consumer<Song> handleInsertedSong) {
        Song song = findSong(file);
        if (song == null) {
            song = new Song(songName, songArtistId, albumId, track, file);
            song.length = songLength;
            insertSong(song);
            Observable.just(song).subscribe(handleInsertedSong).dispose();
        }
        return song;
    }

    /**
     * Inserts a new music path into the DB. It is checked if this path exists.
     * @param path The music path to insert into the DB.
     * @return true if the path was successfully inserted, false otherwise.
     */
    public boolean insertMusicPath(File path) {
        try {
            final File canonicalPath = path.getCanonicalFile();
            if (canonicalPath.isDirectory()) {
                insertMusicPath(canonicalPath.getCanonicalPath());
                return true;
            } else {
                Log.e(getClass().getName(), "Path '" + path + "' is not a valid directory.");
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error while checking path: " + e.getMessage());
        }
        return false;
    }

    /**
     * This helper method should only be called once, right after the app has been installed for
     * the first time. Should be called after READ_EXTERNAL_STORAGE has been granted and before
     * the FileScannerService is launched for the first time.
     */
    public void initDefaultMusicDirectory() {
        checkInsertMusicPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        if (System.getenv("SECONDARY_STORAGE") != null) {
            checkInsertMusicPath(System.getenv("SECONDARY_STORAGE") + "/" + Environment.DIRECTORY_MUSIC);
        }
    }

    public void deleteAllMusic() {
        deleteAllArtists();
        deleteAllAlbums();
        deleteAllImages();
        deleteAllSongs();
    }

    /**
     * Checks if a path is an existing directory and if so inserts it into the DB as default music
     * directory.
     *
     * @param path The path to check.
     */
    private void checkInsertMusicPath(String path) {
        checkInsertMusicPath(new File(path));
    }

    /**
     * Checks if a path is an existing directory and if so inserts it into the DB as default music
     * directory.
     *
     * @param path The path to check.
     */
    private void checkInsertMusicPath(File path) {
        try {
            final File canonicalPath = path.getCanonicalFile();
            if (canonicalPath.isDirectory() && canonicalPath.listFiles() != null) {
                insertMusicPath(canonicalPath.getCanonicalPath());
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error while checking path: " + e.getMessage());
        }
    }

    /**
     * Inserts the given path as a default music directory into the DB.
     *
     * @param path The path to insert into the DB.
     */
    private void insertMusicPath(String path) {
        final Directory directory = new Directory(path);
        insertDirectory(directory);
    }

    private Long getCurrentSongId() {
        ApplicationState stateSongId = getApplicationState(APPSTATE_CURRENT_SONG_ID.name());
        if (stateSongId != null) {
            return Long.parseLong(stateSongId.value);
        } else {
            return null;
        }
    }

    public Song getCurrentSong() {
        final Long songId = getCurrentSongId();
        return songId == null ? null : findSong(songId);
    }

    private void setCurrentSongId(Integer songId) {
        if (songId == null) {
            deleteApplicationState(APPSTATE_CURRENT_SONG_ID.name());
        } else {
            ApplicationState stateSongId = getApplicationState(APPSTATE_CURRENT_SONG_ID.name());
            if (stateSongId == null) {
                stateSongId = new ApplicationState(APPSTATE_CURRENT_SONG_ID.name(), String.valueOf(songId));
                insertApplicationState(stateSongId);
            } else {
                stateSongId.value = String.valueOf(songId);
                updateApplicationState(stateSongId);
            }
        }
    }

    public void setCurrentSong(Song song) {
        setCurrentSongId(song == null ? null : song.id);
    }

    public PlayStatus getCurrentPlayStatus() {
        ApplicationState statePlayStatus = getApplicationState(APPSTATE_CURRENT_PLAY_STATUS.name());
        if (statePlayStatus != null) {
            return PlayStatus.valueOf(statePlayStatus.value);
        } else {
            return PlayStatus.STOPPED;
        }
    }

    public void setCurrentPlayStatus(PlayStatus playStatus) {
        if (playStatus == null) {
            deleteApplicationState(APPSTATE_CURRENT_PLAY_STATUS.name());
        } else {
            ApplicationState statePlayStatus = getApplicationState(APPSTATE_CURRENT_PLAY_STATUS.name());
            if (statePlayStatus == null) {
                statePlayStatus = new ApplicationState(APPSTATE_CURRENT_PLAY_STATUS.name(), playStatus.name());
                insertApplicationState(statePlayStatus);
            } else {
                statePlayStatus.value = playStatus.name();
                updateApplicationState(statePlayStatus);
            }
        }
    }

    public PlayMode getCurrentPlayMode() {
        ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_PLAY_MODE.name());
        if (statePlayMode != null) {
            return PlayMode.valueOf(statePlayMode.value);
        } else {
            return PlayMode.NO_REPEAT;
        }
    }

    public void setCurrentPlayMode(PlayMode playMode) {
        if (playMode == null) {
            deleteApplicationState(APPSTATE_CURRENT_PLAY_MODE.name());
        } else {
            ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_PLAY_MODE.name());
            if (statePlayMode == null) {
                statePlayMode = new ApplicationState(APPSTATE_CURRENT_PLAY_MODE.name(), playMode.name());
                insertApplicationState(statePlayMode);
            } else {
                statePlayMode.value = playMode.name();
                updateApplicationState(statePlayMode);
            }
        }
    }

    public long getCurrentMillis() {
        ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_MILLIS.name());
        if (statePlayMode != null) {
            return Long.parseLong(statePlayMode.value);
        } else {
            return 0;
        }
    }

    public void setCurrentMillis(long millis) {
        ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_MILLIS.name());
        if (statePlayMode == null) {
            statePlayMode = new ApplicationState(APPSTATE_CURRENT_MILLIS.name(), String.valueOf(millis));
            insertApplicationState(statePlayMode);
        } else {
            statePlayMode.value = String.valueOf(millis);
            updateApplicationState(statePlayMode);
        }
    }
}
