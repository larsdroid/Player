package org.willemsens.player.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.willemsens.player.model.Directory;
import org.willemsens.player.model.Models;

import io.requery.android.sqlite.DatabaseSource;

public class MusicDatabaseSource extends DatabaseSource {
    private static final int DB_VERSION = 1;

    public MusicDatabaseSource(Context context) {
        super(context, Models.DEFAULT, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);

        db.execSQL("INSERT INTO " + Directory.$TYPE.getName() +
                " (" + Directory.PATH.getName() + ")" +
                " VALUES ('/storage/sdcard/Music')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
