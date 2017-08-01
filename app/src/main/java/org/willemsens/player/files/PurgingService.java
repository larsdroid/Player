package org.willemsens.player.files;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * A background service that checks all entries of the music DB to see if they still exist,
 * purging all non-existing entries from the DB if necessary.
 */
public class PurgingService extends IntentService {
    public PurgingService() {
        super(PurgingService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // The intent should not have any parameters in it (is this good practice)?
        // Artist per Artist should be iterated over or Album per Album?
        // --> THEN song per song.
        // Afterwards, each album should be checked to see if there is still at least one song,
        // if not -> purge album
        // Afterwards, each artist should be checked to see if there is still at least one album
        // OR song, if not -> purge artist
    }
}
