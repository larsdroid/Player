package org.willemsens.player.files;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
    public FileScannerService() {
        super(FileScannerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Intent should have one parameter: the directory to scan
        // Scanning should start here
        // A reference to the music DB should be had here --> update the music DB while scanning
    }
}
