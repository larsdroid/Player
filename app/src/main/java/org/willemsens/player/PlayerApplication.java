package org.willemsens.player;

import android.app.Application;
import android.os.StrictMode;
import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;
import org.willemsens.player.model.Models;

public class PlayerApplication extends Application {
    private EntityDataStore<Persistable> dataStore;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }

    public EntityDataStore<Persistable> getData() {
        if (dataStore == null) {
            // TODO: override onUpgrade in class DatabaseSource
            DatabaseSource source = new DatabaseSource(this, Models.DEFAULT, 1);
            if (BuildConfig.DEBUG) {
                source.setTableCreationMode(TableCreationMode.DROP_CREATE);
            }
            Configuration configuration = source.getConfiguration();
            dataStore = new EntityDataStore<>(configuration);
        }
        return dataStore;
    }
}
