package org.willemsens.player.services;

import android.app.IntentService;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.persistence.MusicDao;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public abstract class ImageFetcherService extends IntentService {
    private static final int WAIT_MILLIS = 1100;

    private MusicDao musicDao;

    protected ImageFetcherService(String serviceName) {
        super(serviceName);
    }

    protected MusicDao getMusicDao() {
        if (this.musicDao == null) {
            final EntityDataStore<Persistable> dataStore = ((PlayerApplication)getApplication()).getData();
            this.musicDao = new MusicDao(dataStore);
        }
        return this.musicDao;
    }

    protected void waitRateLimit() {
        try {
            Thread.sleep(WAIT_MILLIS);
        }
        catch (InterruptedException e) {
            Log.d(getClass().getName(), e.getMessage());
        }
    }
}
