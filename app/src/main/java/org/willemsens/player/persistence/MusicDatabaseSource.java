package org.willemsens.player.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Models;

import java.io.File;
import java.io.IOException;

import io.requery.android.sqlite.DatabaseSource;

public class MusicDatabaseSource extends DatabaseSource {
    private static final int DB_VERSION = 1;

    public MusicDatabaseSource(Context context) {
        super(context, Models.DEFAULT, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);

        checkInsertMusicPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), db);
        if (System.getenv("SECONDARY_STORAGE") != null) {
            checkInsertMusicPath(System.getenv("SECONDARY_STORAGE") + "/" + Environment.DIRECTORY_MUSIC, db);
        }
    }

    /**
     * Checks if a path is an existing directory and if so inserts it into the DB as default music
     * directory.
     * @param path The path to check.
     * @return 'true' in case the path was inserted into the DB. 'false' otherwise.
     */
    private boolean checkInsertMusicPath(String path, SQLiteDatabase db) {
        return checkInsertMusicPath(new File(path), db);
    }

    /**
     * Checks if a path is an existing directory and if so inserts it into the DB as default music
     * directory.
     * @param path The path to check.
     * @return 'true' in case the path was inserted into the DB. 'false' otherwise.
     */
    private boolean checkInsertMusicPath(File path, SQLiteDatabase db) {
        try {
            final File canonicalPath = path.getCanonicalFile();
            if (canonicalPath.isDirectory()) {
                insertMusicPath(canonicalPath.getCanonicalPath(), db);
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
    private void insertMusicPath(String path, SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Directory.$TYPE.getName() +
                " (" + Directory.PATH.getName() + ")" +
                " VALUES ('" + path + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
