package org.willemsens.player.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import org.willemsens.player.BuildConfig;
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
    private static volatile AppDatabase INSTANCE;

    public abstract MusicDao musicDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    if (BuildConfig.DEBUG) {
                        INSTANCE =
                                Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database")
                                        .build();
                    } else {
                        INSTANCE =
                                Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database")
                                        .allowMainThreadQueries()
                                        .build();
                    }
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
