package org.willemsens.player.files;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import org.willemsens.player.model.Directory;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
    private MusicDao musicDao;

    public FileScannerService() {
        super(FileScannerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        checkMusicDao();

        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.getPath()).getCanonicalFile();
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
        File[] files = currentRoot.listFiles();
        if (files != null) {
            for (File file : files) {
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

    private void checkMusicDao() {
        if (this.musicDao == null) {
            this.musicDao = new MusicDao(getApplicationContext());
        }
    }
}
