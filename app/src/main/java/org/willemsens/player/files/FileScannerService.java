package org.willemsens.player.files;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import org.willemsens.player.R;

import java.io.File;
import java.io.IOException;

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
        if (intent != null && intent.getStringExtra(getString(R.string.key_scan_directory)) != null) {
            final String directoryPath = intent.getStringExtra(getString(R.string.key_scan_directory));

            File root = new File(directoryPath);
            try {
                root = root.getCanonicalFile();
                if (root.isDirectory()) {
                    processDirectory(root);
                } else {
                    Log.e(getClass().getName(), root.getAbsolutePath() + " is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processDirectory(File currentRoot) throws IOException {
        for (File file : currentRoot.listFiles()) {
            final File canonicalFile = file.getCanonicalFile();
            if (canonicalFile.isDirectory()) {
                processDirectory(canonicalFile);
            } else {
                // TODO: process file: ID3 tag and submit to directory
                Log.d(getClass().getName(), "Found " + canonicalFile.getAbsolutePath());
            }
        }
    }
}
