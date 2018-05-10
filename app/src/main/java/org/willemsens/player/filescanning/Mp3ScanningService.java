package org.willemsens.player.filescanning;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import org.willemsens.mp3_vbr_length.Mp3Info;
import org.willemsens.player.model.Song;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastBuilder;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;

import static org.willemsens.player.filescanning.Mp3ScanningPayloadType.MP3PT_SONG_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_SONG_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_SONG_UPDATED;

/**
 * A background service that scans all MP3 files front-to-back to determine their length.
 * It's the only bullet-proof way to obtain a correct song length for VBR (variable bit-rate)
 * MP3 files.
 */
public class Mp3ScanningService extends IntentService {
    private MusicDao musicDao;

    public Mp3ScanningService() {
        super(Mp3ScanningService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (this.musicDao == null) {
            this.musicDao = AppDatabase.getAppDatabase(this).musicDao();
        }

        if (intent != null && intent.hasExtra(MP3PT_SONG_ID.name())) {
            final Song song = this.musicDao.findSong(intent.getLongExtra(MP3PT_SONG_ID.name(), -1));
            if (song != null) {
                scanMp3File(song);
            } else {
                Log.e(getClass().getName(), "Can't find MP3 file to scan. Song ID: \"" + intent.getLongExtra(MP3PT_SONG_ID.name(), -1) + "\"");
            }
        } else {
            scanMp3Files();
        }
    }

    private void scanMp3File(Song song) {
        try {
            // This takes a while!!
            final Mp3Info mp3Info = Mp3Info.of(new File(song.file));

            song.length = mp3Info.getSeconds();
            this.musicDao.updateSong(song);

            MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this);
            builder
                    .setType(MLBT_SONG_UPDATED)
                    .setRecordId(MLBPT_SONG_ID, song.id)
                    .buildAndSubmitBroadcast();
        } catch (IOException e) {
            Log.e(getClass().getName(), "Can't create File object: \"" + e.getMessage() + "\"");
        }
    }

    private void scanMp3Files() {
        for (Song song : this.musicDao.getAllSongsMissingLength()) {
            scanMp3File(song);
        }
    }
}
