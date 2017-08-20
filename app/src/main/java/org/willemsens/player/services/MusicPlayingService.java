package org.willemsens.player.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.notification.NotificationBar;
import org.willemsens.player.notification.NotificationType;
import org.willemsens.player.persistence.MusicDao;

import java.io.IOException;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class MusicPlayingService extends Service
        implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private MusicDao musicDao;
    private MediaPlayer mediaPlayer;
    private Song currentSong;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (this.musicDao == null) {
                final EntityDataStore<Persistable> dataStore = ((PlayerApplication) getApplication()).getData();
                this.musicDao = new MusicDao(dataStore);
            }

            if (this.mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
                this.mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

                final AudioAttributes.Builder builder = new AudioAttributes.Builder();
                builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA);
                final AudioAttributes attributes = builder.build();
                this.mediaPlayer.setAudioAttributes(attributes);
            }

            final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
            final Song song = this.musicDao.findSong(songId);

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            if (!song.equals(currentSong)) {
                mediaPlayer.reset();
            }

            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.path(song.getFile()).build();
            this.currentSong = song;

            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Log.e(getClass().getName(), "Can't play song: " + e.getMessage());
            }
        }
        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();

        /*Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotif, PendingIntent.FLAG_UPDATE_CURRENT);*/
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setAutoCancel(false)
                .setOngoing(true)
                //.setContentIntent(pendingIntent)
                .setContent(new NotificationBar(getApplicationContext().getPackageName()));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationType.DEFAULT_START.getCode(), builder.build());
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

        if (this.musicDao != null) {
            this.musicDao = null;
        }
    }
}
