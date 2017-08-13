package org.willemsens.player.services;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;

import java.io.IOException;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class MusicPlayingService extends IntentService implements MediaPlayer.OnErrorListener {
    private MusicDao musicDao;
    private MediaPlayer mediaPlayer;
    private Song currentSong;

    public MusicPlayingService() {
        super(MusicPlayingService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            if (this.musicDao == null) {
                final EntityDataStore<Persistable> dataStore = ((PlayerApplication)getApplication()).getData();
                this.musicDao = new MusicDao(dataStore);
            }

            if (this.mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
                this.mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            }

            final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
            final Song song = this.musicDao.findSong(songId);
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.path(song.getFile()).build();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
                mediaPlayer.prepare();
                mediaPlayer.start();
                this.currentSong = song;
            } catch (IOException e) {
                Log.e(getClass().getName(), "Can't play song: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onDestroy() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }
}
