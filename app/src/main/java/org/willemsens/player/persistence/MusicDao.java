package org.willemsens.player.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.willemsens.player.persistence.MusicContract.AlbumEntry;
import static org.willemsens.player.persistence.MusicContract.ArtistEntry;
import static org.willemsens.player.persistence.MusicContract.DirectoryEntry;
import static org.willemsens.player.persistence.MusicContract.SongEntry;

public class MusicDao {
    private final SQLiteDatabase database;
    private final MusicDbHelper dbHelper;

    public MusicDao(Context context) {
        dbHelper = new MusicDbHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Directory> getAllDirectories() {
        ArrayList<Directory> directories = new ArrayList<>();

        Cursor cursor = database.query(DirectoryEntry.TABLE_NAME, DirectoryEntry.ALL_COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Directory directory = cursorToDirectory(cursor);
            directories.add(directory);
            cursor.moveToNext();
        }
        cursor.close();

        return directories;
    }

    private Directory cursorToDirectory(Cursor cursor) {
        return new Directory(cursor.getLong(0), cursor.getString(1), cursor.isNull(2) ? null : cursor.getString(2));
    }

    public List<Album> getAllAlbums() {
        ArrayList<Album> albums = new ArrayList<>();

        Cursor cursor = database.query(AlbumEntry.TABLE_NAME, AlbumEntry.ALL_COLUMNS, null, null,
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Album album = cursorToAlbum(cursor);
            albums.add(album);
            cursor.moveToNext();
        }
        cursor.close();

        return albums;
    }

    private List<Artist> getAllArtists() {
        ArrayList<Artist> artists = new ArrayList<>();

        Cursor cursor = database.query(ArtistEntry.TABLE_NAME, ArtistEntry.ALL_COLUMNS, null, null,
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Artist artist = cursorToArtist(cursor);
            artists.add(artist);
            cursor.moveToNext();
        }
        cursor.close();

        return artists;
    }

    private Album cursorToAlbum(Cursor cursor) {
        // TODO: FK, refer to Album (or its ID?)
        return new Album(cursor.getLong(0), cursor.getString(1), null/*artist*/, cursor.getInt(3), cursor.getInt(4));
    }

    private Artist cursorToArtist(Cursor cursor) {
        return new Artist(cursor.getLong(0), cursor.getString(1));
    }

    private Song cursorToSong(Cursor cursor) {
        // TODO: FK, refer to Album (or its ID?)
        return new Song(cursor.getLong(0), cursor.getString(1), null/*artist*/, null/*album*/, cursor.getInt(4), cursor.getString(5));
    }

    /**
     * TODO: merge 'removeExistingAlbums' and 'insertAlbums' into a single method that handles NEW AND EXISTING Albums.
     *       The new method can work internally with private methods.
     * TODO: same for Artists
     *
     * Removes the albums that exist in the DB from the given set. Also updates the ID of the removed Album.
     * @param albums The set to remove albums from in case they exist in the DB.
     */
    public void removeExistingAlbums(Set<Album> albums) {
        for (Album databaseAlbum : getAllAlbums()) {
            if (albums.contains(databaseAlbum)) {
                for (Album setAlbum : albums) {
                    if (setAlbum.equals(databaseAlbum)) {
                        setAlbum.setId(databaseAlbum.getId());
                        break;
                    }
                }
                albums.remove(databaseAlbum);
            }
        }
    }

    public void insertAlbums(Set<Album> albums) {
        for (Album album : albums) {
            ContentValues values = new ContentValues();
            values.put(AlbumEntry.COLUMN_NAME_NAME, album.getName());
            if (album.getArtist() == null) {
                values.putNull(AlbumEntry.COLUMN_NAME_ARTIST);
            } else {
                values.put(AlbumEntry.COLUMN_NAME_ARTIST, album.getArtist().getId());
            }
            if (album.getYear() == null) {
                values.putNull(AlbumEntry.COLUMN_NAME_YEAR);
            } else {
                values.put(AlbumEntry.COLUMN_NAME_YEAR, album.getYear());
            }
            values.put(AlbumEntry.COLUMN_NAME_LENGTH, album.getLength());
            long id = database.insert(AlbumEntry.TABLE_NAME, null, values);
            album.setId(id);

            Log.d(getClass().getName(), "Inserted Album: " + album);
        }
    }

    /**
     * Removes the artists that exist in the DB from the given set. Also updates the ID of the removed Artist.
     * @param artists The set to remove artists from in case they exist in the DB.
     */
    public void removeExistingArtists(Set<Artist> artists) {
        for (Artist databaseArtist : getAllArtists()) {
            if (artists.contains(databaseArtist)) {
                for (Artist setArtist : artists) {
                    if (setArtist.equals(databaseArtist)) {
                        setArtist.setId(databaseArtist.getId());
                        break;
                    }
                }
                artists.remove(databaseArtist);
            }
        }
    }

    public void insertArtists(Set<Artist> artists) {
        for (Artist artist : artists) {
            ContentValues values = new ContentValues();
            values.put(ArtistEntry.COLUMN_NAME_NAME, artist.getName());
            long id = database.insert(ArtistEntry.TABLE_NAME, null, values);
            artist.setId(id);

            Log.d(getClass().getName(), "Inserted Artist: " + artist);
        }
    }

    /**
     * Checks songs in the DB. If a song exists, its ID is updated in the POJO. If the song doesn't
     * exist, it's inserted in the DB and the new ID is set in the POJO.
     * @param songs The set of songs to check for in the DB.
     */
    public void checkSongsSelectInsert(Set<Song> songs) {
        Song dbSong;
        for (Song song : songs) {
            dbSong = findSong(song);
            if (dbSong == null) {
                insertSong(song);
            } else {
                song.setId(dbSong.getId());
            }
        }
    }

    private Song findSong(Song song) {
        Song result = null;

        Cursor cursor = database.query(SongEntry.TABLE_NAME, SongEntry.ALL_COLUMNS,
                SongEntry.COLUMN_NAME_FILE + " = ?", new String[] { song.getFile() },
                null, null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            result = cursorToSong(cursor);
        }
        cursor.close();

        return result;
    }

    /**
     * Inserts the Song in the DB and sets the Song object's ID to the new ID from the DB.
     * @param song The song to insert.
     */
    private void insertSong(Song song) {
        ContentValues values = new ContentValues();
        values.put(SongEntry.COLUMN_NAME_NAME, song.getName());
        values.put(SongEntry.COLUMN_NAME_ARTIST, song.getArtist().getId());
        if (song.getAlbum() == null) {
            values.putNull(SongEntry.COLUMN_NAME_ALBUM);
        } else {
            values.put(SongEntry.COLUMN_NAME_ALBUM, song.getAlbum().getId());
        }
        values.put(SongEntry.COLUMN_NAME_LENGTH, song.getLength());
        values.put(SongEntry.COLUMN_NAME_FILE, song.getFile());
        long id = database.insert(SongEntry.TABLE_NAME, null, values);
        song.setId(id);

        Log.d(getClass().getName(), "Inserted Song: " + song);
    }
}
