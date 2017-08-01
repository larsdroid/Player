package org.willemsens.player.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static org.willemsens.player.persistence.MusicContract.ArtistEntry;
import static org.willemsens.player.persistence.MusicContract.AlbumEntry;
import static org.willemsens.player.persistence.MusicContract.SongEntry;
import static org.willemsens.player.persistence.MusicContract.DirectoryEntry;

public class MusicDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Music.db";

    private static final String[] SQL_CREATE_STATEMENTS = {
            "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                    ArtistEntry._ID + " INTEGER PRIMARY KEY," +
                    ArtistEntry.COLUMN_NAME_NAME + " TEXT)",
            "CREATE TABLE " + AlbumEntry.TABLE_NAME + " (" +
                    AlbumEntry._ID + " INTEGER PRIMARY KEY," +
                    AlbumEntry.COLUMN_NAME_NAME + " TEXT," +
                    AlbumEntry.COLUMN_NAME_ARTIST + " INTEGER," + // TODO: FK
                    AlbumEntry.COLUMN_NAME_YEAR + " INTEGER," +
                    AlbumEntry.COLUMN_NAME_LENGTH + " INTEGER," +   // In seconds
                    "FOREIGN KEY (" + AlbumEntry.COLUMN_NAME_ARTIST + ") REFERENCES " +
                        ArtistEntry.TABLE_NAME + "(" + ArtistEntry._ID + "))",
            "CREATE TABLE " + SongEntry.TABLE_NAME + " (" +
                    SongEntry._ID + " INTEGER PRIMARY KEY," +
                    SongEntry.COLUMN_NAME_NAME + " TEXT," +
                    SongEntry.COLUMN_NAME_ARTIST + " INTEGER," +
                    SongEntry.COLUMN_NAME_ALBUM + " INTEGER," +
                    SongEntry.COLUMN_NAME_LENGTH + " TEXT," +       // In seconds
                    "FOREIGN KEY (" + SongEntry.COLUMN_NAME_ARTIST + ") REFERENCES " +
                        ArtistEntry.TABLE_NAME + "(" + ArtistEntry._ID + ")," +
                    "FOREIGN KEY (" + SongEntry.COLUMN_NAME_ALBUM + ") REFERENCES " +
                        AlbumEntry.TABLE_NAME + "(" + AlbumEntry._ID + "))",
            "CREATE TABLE " + DirectoryEntry.TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY," +
                    DirectoryEntry.COLUMN_NAME_PATH + " TEXT," +
                    // Datetime in ISO 8601: YYYY-MM-DDThh:mm:ssZ
                    DirectoryEntry.COLUMN_NAME_SCAN_TIMESTAMP + " TEXT)"
    };

    private static final String[] SQL_DELETE_STATEMENTS = {
            "DROP TABLE IF EXISTS " + DirectoryEntry.TABLE_NAME,
            "DROP TABLE IF EXISTS " + SongEntry.TABLE_NAME,
            "DROP TABLE IF EXISTS " + AlbumEntry.TABLE_NAME,
            "DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME
    };

    public MusicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String createStatement : SQL_CREATE_STATEMENTS) {
            db.execSQL(createStatement);
        }

        // TODO: Add first default entry:
        // File musicDirectory = new File( getExternalFilesDir(Environment.DIRECTORY_MUSIC));
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
