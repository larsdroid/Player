package org.willemsens.player.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.os.Environment;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_MILLIS;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_PLAY_MODE;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_PLAY_STATUS;
import static org.willemsens.player.persistence.ApplicationStateProperty.APPSTATE_CURRENT_SONG_ID;

@Dao
public interface MusicDao {
    @Query("SELECT * FROM directory")
    List<Directory> getAllDirectories();

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

    public Song findNextSong(Song song) {
        return this.dataStore.select(Song.class)
                .where(Song.ALBUM.equal(song.getAlbum())
                        .and(Song.TRACK.greaterThan(song.getTrack())))
                .orderBy(Song.TRACK.asc())
                .get().firstOrNull();
    }

    public Song findPreviousSong(Song song) {
        return this.dataStore.select(Song.class)
                .where(Song.ALBUM.equal(song.getAlbum())
                        .and(Song.TRACK.lessThan(song.getTrack())))
                .orderBy(Song.TRACK.desc())
                .get().firstOrNull();
    }

    // TODO: isn't it possible to just do 'album.getSongs()'? Check requery!
    public List<Song> getAllSongs(Album album) {
        return this.dataStore.select(Song.class)
                .where(Song.ALBUM.equal(album))
                .orderBy(Song.TRACK.asc())
                .get().toList();
    }

    public Song findFirstSong(Album album) {
        return this.dataStore.select(Song.class)
                .where(Song.ALBUM.equal(album))
                .orderBy(Song.TRACK.asc())
                .get().firstOrNull();
    }

    public Song findLastSong(Album album) {
        return this.dataStore.select(Song.class)
                .where(Song.ALBUM.equal(album))
                .orderBy(Song.TRACK.desc())
                .get().firstOrNull();
    }

    public List<Long> insertAlbumsIfNotExist(Set<Album> albums) {
        final List<Long> insertedIds = new ArrayList<>();
        for (Album album : albums) {
            if (album.getId() == null) {
                if (album.getArtist().getId() == null) {
                    Artist dbArtist = this.dataStore.select(Artist.class)
                            .where(Artist.NAME.equal(album.getArtist().getName()))
                            .get().firstOrNull();
                    album.setArtist(dbArtist);
                }
                Album dbAlbum = this.dataStore.select(Album.class)
                        .where(Album.NAME.equal(album.getName()))
                        .and(Album.ARTIST.equal(album.getArtist()))
                        .get().firstOrNull();
                if (dbAlbum == null) {
                    insertAlbum(album);
                    insertedIds.add(album.getId());
                }
            }
        }
        return insertedIds;
    }

    public List<Long> insertArtistsIfNotExist(Set<Artist> artists) {
        final List<Long> insertedIds = new ArrayList<>();
        for (Artist artist : artists) {
            if (artist.getId() == null) {
                Artist dbArtist = this.dataStore.select(Artist.class)
                        .where(Artist.NAME.equal(artist.getName()))
                        .get().firstOrNull();
                if (dbArtist == null) {
                    insertArtist(artist);
                    insertedIds.add(artist.getId());
                }
            }
        }
        return insertedIds;
    }

    public List<Long> insertSongsIfNotExist(Set<Song> songs) {
        final List<Long> insertedIds = new ArrayList<>();
        for (Song song : songs) {
            if (song.getId() == null) {
                if (song.getArtist().getId() == null) {
                    Artist dbArtist = this.dataStore.select(Artist.class)
                            .where(Artist.NAME.equal(song.getArtist().getName()))
                            .get().firstOrNull();
                    song.setArtist(dbArtist);
                }
                if (song.getAlbum().getId() == null) {
                    Album dbAlbum = this.dataStore.select(Album.class)
                            .where(Album.NAME.equal(song.getName()))
                            .and(Album.ARTIST.equal(song.getArtist()))
                            .get().firstOrNull();
                    song.setAlbum(dbAlbum);
                }
                Song dbSong = this.dataStore.select(Song.class)
                        .where(Song.FILE.equal(song.getFile()))
                        .get().firstOrNull();
                if (dbSong == null) {
                    insertSong(song);
                    insertedIds.add(song.getId());
                }
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

    public void insertImage(Image image) {
        this.dataStore.insert(image);
        Log.v(getClass().getName(), "Inserted Image: " + image);
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

    public void deleteDirectory(Directory directory) {
        this.dataStore.delete(directory);
    }

    public void deleteAllMusic() {
        this.dataStore.delete(Artist.class).get().value();
        this.dataStore.delete(Album.class).get().value();
        this.dataStore.delete(Image.class).get().value();
        this.dataStore.delete(Song.class).get().value();
    }

    public void deleteAllDirectories() {
        this.dataStore.delete(Directory.class).get().value();
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
        final Directory directory = new Directory();
        directory.setPath(path);
        this.dataStore.insert(directory);
    }

    private ApplicationState getApplicationState(String property) {
        return this.dataStore.select(ApplicationState.class)
                .where(ApplicationState.PROPERTY.equal(property))
                .get().firstOrNull();
    }

    private Long getCurrentSongId() {
        ApplicationState stateSongId = getApplicationState(APPSTATE_CURRENT_SONG_ID.name());
        if (stateSongId != null && stateSongId.getValue() != null) {
            return Long.parseLong(stateSongId.getValue());
        } else {
            return null;
        }
    }

    public Song getCurrentSong() {
        final Long songId = getCurrentSongId();
        return songId == null ? null : findSong(songId);
    }

    private void setCurrentSongId(Long songId) {
        if (songId == null) {
            this.dataStore.delete(ApplicationState.class)
                    .where(ApplicationState.PROPERTY.equal(APPSTATE_CURRENT_SONG_ID.name()));
        } else {
            ApplicationState stateSongId = getApplicationState(APPSTATE_CURRENT_SONG_ID.name());
            if (stateSongId == null) {
                stateSongId = new ApplicationState();
                stateSongId.setProperty(APPSTATE_CURRENT_SONG_ID.name());
                stateSongId.setValue(String.valueOf(songId));
                this.dataStore.insert(stateSongId);
            } else {
                stateSongId.setValue(String.valueOf(songId));
                this.dataStore.update(stateSongId);
            }
        }
    }

    public void setCurrentSong(Song song) {
        setCurrentSongId(song == null ? null : song.getId());
    }

    public PlayStatus getCurrentPlayStatus() {
        ApplicationState statePlayStatus = getApplicationState(APPSTATE_CURRENT_PLAY_STATUS.name());
        if (statePlayStatus != null && statePlayStatus.getValue() != null) {
            return PlayStatus.valueOf(statePlayStatus.getValue());
        } else {
            return PlayStatus.STOPPED;
        }
    }

    public void setCurrentPlayStatus(PlayStatus playStatus) {
        if (playStatus == null) {
            this.dataStore.delete(ApplicationState.class)
                    .where(ApplicationState.PROPERTY.equal(APPSTATE_CURRENT_PLAY_STATUS.name()));
        } else {
            ApplicationState statePlayStatus = getApplicationState(APPSTATE_CURRENT_PLAY_STATUS.name());
            if (statePlayStatus == null) {
                statePlayStatus = new ApplicationState();
                statePlayStatus.setProperty(APPSTATE_CURRENT_PLAY_STATUS.name());
                statePlayStatus.setValue(playStatus.name());
                this.dataStore.insert(statePlayStatus);
            } else {
                statePlayStatus.setValue(playStatus.name());
                this.dataStore.update(statePlayStatus);
            }
        }
    }

    public PlayMode getCurrentPlayMode() {
        ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_PLAY_MODE.name());
        if (statePlayMode != null && statePlayMode.getValue() != null) {
            return PlayMode.valueOf(statePlayMode.getValue());
        } else {
            return PlayMode.NO_REPEAT;
        }
    }

    public void setCurrentPlayMode(PlayMode playMode) {
        if (playMode == null) {
            this.dataStore.delete(ApplicationState.class)
                    .where(ApplicationState.PROPERTY.equal(APPSTATE_CURRENT_PLAY_MODE.name()));
        } else {
            ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_PLAY_MODE.name());
            if (statePlayMode == null) {
                statePlayMode = new ApplicationState();
                statePlayMode.setProperty(APPSTATE_CURRENT_PLAY_MODE.name());
                statePlayMode.setValue(playMode.name());
                this.dataStore.insert(statePlayMode);
            } else {
                statePlayMode.setValue(playMode.name());
                this.dataStore.update(statePlayMode);
            }
        }
    }

    public long getCurrentMillis() {
        ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_MILLIS.name());
        if (statePlayMode != null && statePlayMode.getValue() != null) {
            return Long.parseLong(statePlayMode.getValue());
        } else {
            return 0;
        }
    }

    public void setCurrentMillis(long millis) {
        ApplicationState statePlayMode = getApplicationState(APPSTATE_CURRENT_MILLIS.name());
        if (statePlayMode == null) {
            statePlayMode = new ApplicationState();
            statePlayMode.setProperty(APPSTATE_CURRENT_MILLIS.name());
            statePlayMode.setValue(String.valueOf(millis));
            this.dataStore.insert(statePlayMode);
        } else {
            statePlayMode.setValue(String.valueOf(millis));
            this.dataStore.update(statePlayMode);
        }
    }
}
