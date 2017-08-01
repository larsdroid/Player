package org.willemsens.player.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.willemsens.player.model.Directory;

import java.util.ArrayList;
import java.util.List;

import static org.willemsens.player.persistence.MusicContract.DirectoryEntry;

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
        return new Directory(cursor.getString(1), cursor.isNull(2) ? null : cursor.getString(2));
    }
}
