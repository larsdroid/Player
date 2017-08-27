package org.willemsens.player.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.willemsens.player.model.Models;

import io.requery.android.sqlite.DatabaseSource;

public class MusicDatabaseSource extends DatabaseSource {
    private static final int DB_VERSION = 1;

    public MusicDatabaseSource(Context context) {
        super(context, Models.DEFAULT, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
