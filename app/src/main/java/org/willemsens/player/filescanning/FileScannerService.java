package org.willemsens.player.filescanning;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.Directory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
    private static final String[] SUPPORTED_FORMATS = {"flac", "mkv", "mp3", "ogg", "wav"};

    private MusicDao musicDao;
    private AudioFileReader audioFileReader;
    private boolean stopProcessing = false;

    public FileScannerService() {
        super(FileScannerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (this.musicDao == null) {
            this.musicDao = AppDatabase.getAppDatabase(this).musicDao();
        }

        if (this.audioFileReader == null) {
            this.audioFileReader = new AudioFileReader(this.musicDao, this);
        }

        scanMediaStoreFiles();
        scanCustomDirectories();
    }

    private void scanMediaStoreFiles(Uri musicUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        String[] selectionArgs = {"%/audio/ui/%"};
        ContentResolver musicResolver = getContentResolver();
        Cursor musicCursor = musicResolver.query(musicUri, proj,
                MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ?",
                selectionArgs, null);

        if (musicCursor != null) {
            if (musicCursor.moveToFirst()) {
                final int fileColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do {
                    final File file = new File(musicCursor.getString(fileColumn));
                    try {
                        final File canonicalFile = file.getCanonicalFile();
                        processSingleFile(canonicalFile);
                    } catch (IOException e) {
                        Log.e("FileScannerService", e.getMessage());
                    }
                } while (!stopProcessing && musicCursor.moveToNext());
            }
            musicCursor.close();
        }
    }

    private void scanMediaStoreFiles() {
        scanMediaStoreFiles(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        scanMediaStoreFiles(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    private void scanCustomDirectories() {
        for (Directory dir : this.musicDao.getAllDirectories()) {
            try {
                File root = new File(dir.path).getCanonicalFile();
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
        final List<File> filesToProcess = new ArrayList<>();
        final List<File> directoriesToProcess = new ArrayList<>();
        directoriesToProcess.add(currentRoot);

        for (int i = 0; i < directoriesToProcess.size() && !stopProcessing; i++) {
            File directory = directoriesToProcess.get(i);

            final File[] files = directory.listFiles();
            int j = 0;
            while (j < files.length && !stopProcessing) {
                final File file = files[j++];
                final File canonicalFile = file.getCanonicalFile();

                if (canonicalFile.isDirectory() && !directoriesToProcess.contains(canonicalFile)) {
                    directoriesToProcess.add(canonicalFile);
                } else if (!filesToProcess.contains(canonicalFile)) {
                    filesToProcess.add(canonicalFile);
                }
            }
        }

        int i = 0;
        while (i < filesToProcess.size() && !stopProcessing) {
            final File file = filesToProcess.get(i++);
            processSingleFile(file);
        }
    }

    private void processSingleFile(File canonicalFile) {
        if (isMusicFile(canonicalFile)) {
            this.audioFileReader.readSong(canonicalFile);
        }
    }

    private boolean isMusicFile(File canonicalFile) {
        if (canonicalFile.isFile()) {
            final String fileName = canonicalFile.getName();
            final int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex + 1 < fileName.length()) {
                final String extension = fileName.substring(dotIndex + 1, fileName.length());
                for (String supportedExtension : SUPPORTED_FORMATS) {
                    if (supportedExtension.equalsIgnoreCase(extension)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        stopProcessing = true;
        super.onDestroy();
    }
}
