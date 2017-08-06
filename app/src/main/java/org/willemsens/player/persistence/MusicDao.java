package org.willemsens.player.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(AlbumEntry.TABLE_NAME
                        + " LEFT OUTER JOIN "
                        + ArtistEntry.TABLE_NAME
                        + " ON ("
                        + AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_ARTIST
                        + " = "
                        + ArtistEntry.TABLE_NAME + "." + ArtistEntry._ID + ")");

        Cursor cursor = queryBuilder.query(database, new String[] {
                AlbumEntry.TABLE_NAME + "." + AlbumEntry._ID,
                AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_NAME,
                AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_YEAR,
                AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_LENGTH,
                ArtistEntry.TABLE_NAME + "." + ArtistEntry._ID,
                ArtistEntry.TABLE_NAME + "." + ArtistEntry.COLUMN_NAME_NAME },
                null, null, null, null, null);

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
        final Artist albumArtist = new Artist(
                cursor.getLong(4),
                cursor.getString(5));
        return new Album(
                cursor.getLong(0),
                cursor.getString(1),
                albumArtist, cursor.isNull(2) ? null : cursor.getInt(2),
                cursor.getInt(3));
    }

    private Artist cursorToArtist(Cursor cursor) {
        return new Artist(cursor.getLong(0), cursor.getString(1));
    }

    private Song cursorToSongLazy(Cursor cursor) {
        return new Song(cursor.getLong(0), cursor.getString(1), null/*artist*/, null/*album*/, cursor.getInt(4), cursor.getString(5));
    }

    private Song cursorToSongEager(Cursor cursor) {
        final Artist albumArtist = new Artist(
                cursor.getLong(10),
                cursor.getString(11));
        final Album album = new Album(
                cursor.getLong(6),
                cursor.getString(7),
                albumArtist,
                cursor.isNull(8) ? null : cursor.getInt(8),
                cursor.getInt(9));
        final Artist songArtist = new Artist(
                cursor.getLong(4),
                cursor.getString(5));
        return new Song(
                cursor.getLong(0),
                cursor.getString(1),
                songArtist,
                album,
                cursor.getInt(2),
                cursor.getString(3));
    }

    /**
     * Checks albums in the DB. If an album exists, its ID is updated in the POJO. If the album doesn't
     * exist, it's inserted in the DB and the new ID is set in the POJO.
     * @param albums The set of albums to check for in the DB.
     */
    public void checkAlbumsSelectInsert(Set<Album> albums) {
        final List<Album> databaseAlbums = getAllAlbums();
        for (Album album : albums) {
            if (databaseAlbums.contains(album)) {
                for (Album databaseAlbum : databaseAlbums) {
                    if (album.equals(databaseAlbum)) {
                        album.setId(databaseAlbum.getId());
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
     * Inserts the Album in the DB and sets the Album object's ID to the new ID from the DB.
     * @param album The album to insert.
     */
    private void insertAlbum(Album album) {
        ContentValues values = new ContentValues();
        values.put(AlbumEntry.COLUMN_NAME_NAME, album.getName());
        values.put(AlbumEntry.COLUMN_NAME_ARTIST, album.getArtist().getId());
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

    /**
     * Checks artists in the DB. If an artist exists, its ID is updated in the POJO. If the artist doesn't
     * exist, it's inserted in the DB and the new ID is set in the POJO.
     * @param artists The set of artists to check for in the DB.
     */
    public void checkArtistsSelectInsert(Set<Artist> artists) {
        final List<Artist> databaseArtists = getAllArtists();
        for (Artist artist : artists) {
            if (databaseArtists.contains(artist)) {
                for (Artist databaseArtist : databaseArtists) {
                    if (artist.equals(databaseArtist)) {
                        artist.setId(databaseArtist.getId());
                        break;
                    }
                }
            } else {
                insertArtist(artist);
            }
        }
    }

    /**
     * Inserts the Artist in the DB and sets the Artist object's ID to the new ID from the DB.
     * @param artist The artist to insert.
     */
    private void insertArtist(Artist artist) {
        ContentValues values = new ContentValues();
        values.put(ArtistEntry.COLUMN_NAME_NAME, artist.getName());
        long id = database.insert(ArtistEntry.TABLE_NAME, null, values);
        artist.setId(id);

        Log.d(getClass().getName(), "Inserted Artist: " + artist);
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

    private List<Song> getAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SongEntry.TABLE_NAME
                + " LEFT OUTER JOIN "
                + ArtistEntry.TABLE_NAME + " AS A1"
                + " ON ("
                + SongEntry.TABLE_NAME + "." + SongEntry.COLUMN_NAME_ARTIST
                + " = "
                + "A1." + ArtistEntry._ID + ")"
                + " LEFT OUTER JOIN "
                + AlbumEntry.TABLE_NAME
                + " ON ("
                + SongEntry.TABLE_NAME + "." + SongEntry.COLUMN_NAME_ALBUM
                + " = "
                + AlbumEntry.TABLE_NAME + "." + AlbumEntry._ID + ")"
                + " LEFT OUTER JOIN "
                + ArtistEntry.TABLE_NAME + " AS A2"
                + " ON ("
                + AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_ARTIST
                + " = "
                + "A2." + ArtistEntry._ID + ")");

        Cursor cursor = queryBuilder.query(database, new String[] {
                        SongEntry.TABLE_NAME + "." + SongEntry._ID,
                        SongEntry.TABLE_NAME + "." + SongEntry.COLUMN_NAME_NAME,
                        SongEntry.TABLE_NAME + "." + SongEntry.COLUMN_NAME_LENGTH,
                        SongEntry.TABLE_NAME + "." + SongEntry.COLUMN_NAME_FILE,
                        "A1." + ArtistEntry._ID,
                        "A1." + ArtistEntry.COLUMN_NAME_NAME,
                        AlbumEntry.TABLE_NAME + "." + AlbumEntry._ID,
                        AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_NAME,
                        AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_YEAR,
                        AlbumEntry.TABLE_NAME + "." + AlbumEntry.COLUMN_NAME_LENGTH,
                        "A2." + ArtistEntry._ID,
                        "A2." + ArtistEntry.COLUMN_NAME_NAME },
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Song song = cursorToSongEager(cursor);
            songs.add(song);
            cursor.moveToNext();
        }
        cursor.close();

        return songs;
    }

    private Song findSong(Song song) {
        Song result = null;

        Cursor cursor = database.query(SongEntry.TABLE_NAME, SongEntry.ALL_COLUMNS,
                SongEntry.COLUMN_NAME_FILE + " = ?", new String[] { song.getFile() },
                null, null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            result = cursorToSongLazy(cursor);
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
