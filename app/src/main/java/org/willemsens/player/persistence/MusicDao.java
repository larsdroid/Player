package org.willemsens.player.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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

    public void/*ToDoList*/ getAllDirectories() {
        /*ToDoList toDoList = new ToDoList();

        Cursor cursor = database.query(ToDoContract.ToDoEntry.TABLE_NAME, ToDoContract.ToDoEntry.ALL_COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ToDoItem item = cursorToToDoItem(cursor);
            toDoList.addItem(item);
            cursor.moveToNext();
        }
        cursor.close();

        return toDoList;*/
    }

    /*private ToDoItem cursorToToDoItem(Cursor cursor) {
        return new ToDoItem(cursor.getLong(0), cursor.getString(1));
    }*/
}
