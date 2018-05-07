package org.willemsens.player.fetchers;

import android.app.IntentService;
import android.util.Log;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

public abstract class InfoFetcherService extends IntentService {
    private static final int WAIT_MILLIS = 1100;

    private MusicDao musicDao;

    protected InfoFetcherService(String serviceName) {
        super(serviceName);
    }

    protected MusicDao getMusicDao() {
        if (this.musicDao == null) {
            this.musicDao = AppDatabase.getAppDatabase(this).musicDao();
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
