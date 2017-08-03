package org.willemsens.player.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Directory;

import java.util.ArrayList;
import java.util.List;

import static org.willemsens.player.persistence.MusicContract.DirectoryEntry;
import static org.willemsens.player.persistence.MusicContract.AlbumEntry;

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

    private Album cursorToAlbum(Cursor cursor) {
        // TODO: FK, refer to Album (or its ID?)
        return new Album(cursor.getLong(0), cursor.getString(1), null, cursor.getInt(3), cursor.getInt(4));
    }

    public boolean albumExists(Album album) {
        final Cursor cursor = database.query(AlbumEntry.TABLE_NAME, AlbumEntry.ALL_COLUMNS,
                AlbumEntry.COLUMN_NAME_NAME + " = ? AND " +
                AlbumEntry.COLUMN_NAME_ARTIST + " = ?",
                new String[] {album.getName(), String.valueOf(album.getArtist().getId())},
                null, null, null);
        final boolean exists = cursor.getCount() != 0;
        cursor.close();
        return exists;
    }
}
