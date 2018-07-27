package org.willemsens.player.filescanning;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import org.willemsens.player.persistence.entities.Directory;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;

/**
 * A background service that checks all music files within a directory (recursively) and creates or
 * updates the file's information in the music DB.
 */
public class FileScannerService extends IntentService {
    private static final String[] SUPPORTED_FORMATS = {"flac", "mkv", "mp3", "ogg", "wav"};

    private MusicDao musicDao;
    private AudioFileReader audioFileReader;

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
                } while (musicCursor.moveToNext());
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
        final File[] files = currentRoot.listFiles();
        if (files != null) {
            for (File file : files) {
                final File canonicalFile = file.getCanonicalFile();
                if (canonicalFile.isDirectory()) {
                    processDirectory(canonicalFile);
                } else {
                    processSingleFile(canonicalFile);
                }
            }
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
}
