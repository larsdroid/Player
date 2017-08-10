package org.willemsens.player;

import android.app.Application;
import android.os.StrictMode;

import org.willemsens.player.persistence.MusicDatabaseSource;

import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;

public class PlayerApplication extends Application {
    private EntityDataStore<Persistable> dataStore;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }

    public EntityDataStore<Persistable> getData() {
        if (dataStore == null) {
            DatabaseSource source = new MusicDatabaseSource(this);
            if (BuildConfig.DEBUG) {
                source.setTableCreationMode(TableCreationMode.DROP_CREATE);
            }
            Configuration configuration = source.getConfiguration();
            dataStore = new EntityDataStore<>(configuration);
        }
        return dataStore;
    }
}
