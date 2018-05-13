package org.willemsens.player.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.ApplicationState;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Directory;
import org.willemsens.player.persistence.entities.Image;
import org.willemsens.player.persistence.entities.Song;

@Database(entities = {
        Album.class,
        ApplicationState.class,
        Artist.class,
        Directory.class,
        Image.class,
        Song.class
},
        version = 1,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract MusicDao musicDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database")
                            // allow queries on the main thread.
                            // TODO: Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
