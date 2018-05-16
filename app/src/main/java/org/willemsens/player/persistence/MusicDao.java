package org.willemsens.player.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.ApplicationState;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Directory;
import org.willemsens.player.persistence.entities.Image;
import org.willemsens.player.persistence.entities.Song;
import org.willemsens.player.persistence.entities.helpers.AlbumWithImageAndArtist;
import org.willemsens.player.persistence.entities.helpers.ArtistWithImage;
import org.willemsens.player.persistence.entities.helpers.SongWithAlbumInfo;
import org.willemsens.player.playback.PlayMode;
import org.willemsens.player.playback.PlayStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_MILLIS;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_PLAY_MODE;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_PLAY_STATUS;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_SONG_ID;

@Dao
public abstract class MusicDao {
    @Query("SELECT * FROM directory")
    public abstract List<Directory> getAllDirectories();

    @Query("SELECT al.* FROM album al, artist ar WHERE al.artistId = ar.id ORDER BY ar.name, al.yearReleased")
    public abstract List<Album> getAllAlbums_NonLive();

    @Query("SELECT * FROM artist ORDER BY name")
    public abstract List<Artist> getAllArtists_NonLive();

    @Query("SELECT so.* FROM song so, album al, artist ar WHERE so.albumId = al.id AND al.artistId = ar.id ORDER BY ar.name, al.yearReleased, al.id, so.track")
    public abstract List<Song> getAllSongs();

    @Query("SELECT * FROM album WHERE imageId IS NULL OR yearReleased IS NULL")
    public abstract List<Album> getAllAlbumsMissingInfo();

    @Query("SELECT * FROM artist WHERE imageId IS NULL")
    public abstract List<Artist> getAllArtistsMissingImage();

    @Query("SELECT * FROM song WHERE length IS NULL")
    public abstract List<Song> getAllSongsMissingLength();

    @Query("SELECT * FROM album WHERE id = :id")
    public abstract Album findAlbum(long id);

    @Query("SELECT * FROM artist WHERE id = :id")
    public abstract Artist findArtist(long id);

    @Query("SELECT * FROM artist WHERE name = :name")
    abstract Artist findArtist(@NonNull String name);

    @Query("SELECT * FROM album WHERE name = :name AND artistId = :artistId")
    abstract Album findAlbum(@NonNull String name, long artistId);

    @Query("SELECT * FROM song WHERE file = :file")
    abstract Song findSong(@NonNull String file);

    @Query("SELECT * FROM song WHERE id = :id")
    public abstract Song findSong(long id);

    @Query("SELECT * FROM image WHERE id = :id")
    public abstract Image findImage(long id);

    @Query("SELECT * FROM song WHERE albumId = :albumId AND track > :previousTrack ORDER BY track ASC LIMIT 1")
    public abstract Song findNextSong(long albumId, int previousTrack);

    @Query("SELECT * FROM song WHERE albumId = :albumId AND track < :followingTrack ORDER BY track DESC LIMIT 1")
    public abstract Song findPreviousSong(long albumId, int followingTrack);

    @Query("SELECT * FROM album WHERE id = :id")
    public abstract LiveData<Album> getAlbum(long id);

    @Query("SELECT ar.* FROM artist ar, album al WHERE ar.id = al.artistId AND al.id = :albumId")
    public abstract LiveData<Artist> getArtistForAlbum(long albumId);

    @Query("SELECT im.* FROM image im, album al WHERE im.id = al.imageId AND al.id = :albumId")
    public abstract LiveData<Image> getImageForAlbum(long albumId);

    @Query("SELECT * FROM song WHERE albumId = :albumId ORDER BY track ASC")
    public abstract LiveData<List<Song>> getAllSongs(long albumId);

    @Query("SELECT so.* FROM song so, applicationstate ap WHERE so.id = ap.value AND ap.property = 'APPSTATE_CURRENT_SONG_ID'")
    public abstract LiveData<Song> getCurrentSong();

    @Query("SELECT ar.id, ar.name, im.imageData FROM artist ar LEFT OUTER JOIN image im ON ar.imageId = im.id ORDER BY ar.name")
    public abstract LiveData<List<ArtistWithImage>> getAllArtistsWithImages();

    @Query("SELECT al.id, al.name, al.yearReleased, al.length, im.imageData, ar.id AS artistId, ar.name AS artistName FROM album al LEFT JOIN artist ar ON al.artistId = ar.id LEFT OUTER JOIN image im ON al.imageId = im.id ORDER BY ar.name, al.yearReleased")
    public abstract LiveData<List<AlbumWithImageAndArtist>> getAllAlbumsWithImages();

    @Query("SELECT * FROM artist ORDER BY name")
    public abstract LiveData<List<Artist>> getAllArtists();

    @Query("SELECT al.* FROM album al LEFT JOIN artist ar ON al.artistId = ar.id ORDER BY ar.name, al.yearReleased")
    public abstract LiveData<List<Album>> getAllAlbums();

    @Query("SELECT so.id, so.name, so.track, so.length, al.name AS albumName, im.imageData AS albumImageData, ar.name AS artistName FROM song so LEFT JOIN album al ON so.albumId = al.id LEFT JOIN artist ar ON so.artistId = ar.id LEFT OUTER JOIN image im ON al.imageId = im.id ORDER BY ar.name, al.yearReleased, al.id, so.track")
    public abstract LiveData<List<SongWithAlbumInfo>> getAllSongsWithAlbumInfo();

    @Query("SELECT * FROM song WHERE albumId = :albumId ORDER BY track ASC LIMIT 1")
    public abstract Song findFirstSong(long albumId);

    @Query("SELECT * FROM song WHERE albumId = :albumId ORDER BY track DESC LIMIT 1")
    public abstract Song findLastSong(long albumId);

    @Query("SELECT * FROM applicationstate WHERE property = :property LIMIT 1")
    abstract ApplicationState getApplicationState(String property);

    @Update
    public abstract void updateAlbum(Album album);

    @Update
    public abstract void updateArtist(Artist artist);

    @Update
    public abstract void updateSong(Song song);

    @Update
    abstract void updateApplicationState(ApplicationState applicationState);

    @Insert
    abstract long insertAlbum(Album album);

    @Insert
    abstract long insertArtist(Artist artist);

    @Insert
    abstract long insertSong(Song song);

    @Insert
    abstract void insertDirectory(Directory directory);

    @Insert
    abstract void insertApplicationState(ApplicationState applicationState);

    @Insert
    public abstract long insertImage(Image image);

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
            artist.id = insertArtist(artist);
            Observable.just(artist).subscribe(handleInsertedArtist).dispose();
        }
        return artist;
    }

    public Album findOrCreateAlbum(String albumName, long artistId, Integer albumYear, Consumer<Album> handleInsertedAlbum) {
        Album album = findAlbum(albumName, artistId);
        if (album == null) {
            album = new Album(albumName, artistId);
            album.yearReleased = albumYear;
            album.id = insertAlbum(album);
            Observable.just(album).subscribe(handleInsertedAlbum).dispose();
        }
        return album;
    }

    public Song findOrCreateSong(@NonNull String songName, long songArtistId, long albumId, int track, @NonNull String file,
                                 Integer songLength, Consumer<Song> handleInsertedSong) {
        Song song = findSong(file);
        if (song == null) {
            song = new Song(songName, songArtistId, albumId, track, file);
            song.length = songLength;
            song.id = insertSong(song);
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
        deleteAllSongs();
        deleteAllAlbums();
        deleteAllArtists();
        deleteAllImages();
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

    private Integer getCurrentSongId() {
        ApplicationState stateSongId = getApplicationState(APPSTATE_CURRENT_SONG_ID.name());
        if (stateSongId != null) {
            return Integer.parseInt(stateSongId.value);
        } else {
            return null;
        }
    }

    public Song getCurrentSong_NonLive() {
        final Integer songId = getCurrentSongId();
        return songId == null ? null : findSong(songId);
    }

    private void setCurrentSongId(Long songId) {
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
