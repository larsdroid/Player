package org.willemsens.player.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static org.willemsens.player.persistence.MusicContract.AlbumEntry;
import static org.willemsens.player.persistence.MusicContract.ArtistEntry;
import static org.willemsens.player.persistence.MusicContract.DirectoryEntry;
import static org.willemsens.player.persistence.MusicContract.SongEntry;

class MusicDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Music.db";

    private static final String[] SQL_CREATE_STATEMENTS = {
            "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                    ArtistEntry._ID + " INTEGER PRIMARY KEY," +
                    ArtistEntry.COLUMN_NAME_NAME + " TEXT NOT NULL)",
            "CREATE TABLE " + AlbumEntry.TABLE_NAME + " (" +
                    AlbumEntry._ID + " INTEGER PRIMARY KEY," +
                    AlbumEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                    AlbumEntry.COLUMN_NAME_ARTIST + " INTEGER NOT NULL," +
                    AlbumEntry.COLUMN_NAME_YEAR + " INTEGER," + // TODO: NOT NULL???
                    AlbumEntry.COLUMN_NAME_LENGTH + " INTEGER NOT NULL," +   // In seconds
                    "FOREIGN KEY (" + AlbumEntry.COLUMN_NAME_ARTIST + ") REFERENCES " +
                        ArtistEntry.TABLE_NAME + "(" + ArtistEntry._ID + "))",
            "CREATE TABLE " + SongEntry.TABLE_NAME + " (" +
                    SongEntry._ID + " INTEGER PRIMARY KEY," +
                    SongEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                    SongEntry.COLUMN_NAME_ARTIST + " INTEGER NOT NULL," +
                    SongEntry.COLUMN_NAME_ALBUM + " INTEGER NOT NULL," +
                    SongEntry.COLUMN_NAME_LENGTH + " TEXT NOT NULL," +       // In seconds
                    SongEntry.COLUMN_NAME_FILE + " TEXT UNIQUE NOT NULL, " +
                    "FOREIGN KEY (" + SongEntry.COLUMN_NAME_ARTIST + ") REFERENCES " +
                        ArtistEntry.TABLE_NAME + "(" + ArtistEntry._ID + ")," +
                    "FOREIGN KEY (" + SongEntry.COLUMN_NAME_ALBUM + ") REFERENCES " +
                        AlbumEntry.TABLE_NAME + "(" + AlbumEntry._ID + "))",
            "CREATE TABLE " + DirectoryEntry.TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY," +
                    DirectoryEntry.COLUMN_NAME_PATH + " TEXT NOT NULL," +
                    // Datetime in ISO 8601: YYYY-MM-DDThh:mm:ssZ
                    DirectoryEntry.COLUMN_NAME_SCAN_TIMESTAMP + " TEXT)"
    };

    private static final String[] SQL_DELETE_STATEMENTS = {
            "DROP TABLE IF EXISTS " + DirectoryEntry.TABLE_NAME,
            "DROP TABLE IF EXISTS " + SongEntry.TABLE_NAME,
            "DROP TABLE IF EXISTS " + AlbumEntry.TABLE_NAME,
            "DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME
    };

    MusicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String createStatement : SQL_CREATE_STATEMENTS) {
            db.execSQL(createStatement);
        }

        db.execSQL("INSERT INTO " + DirectoryEntry.TABLE_NAME +
                " (" + DirectoryEntry.COLUMN_NAME_PATH +
                ", " + DirectoryEntry.COLUMN_NAME_SCAN_TIMESTAMP + ")" +
                " VALUES ('/storage/sdcard/Music', NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: implement proper upgrade...
        for (String dropStatement : SQL_DELETE_STATEMENTS) {
            db.execSQL(dropStatement);
        }
        onCreate(db);
    }
}
