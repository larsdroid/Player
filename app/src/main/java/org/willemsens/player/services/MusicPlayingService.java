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
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.notification.NotificationBar;
import org.willemsens.player.notification.NotificationType;
import org.willemsens.player.persistence.MusicDao;

import java.io.IOException;

public class MusicPlayingService extends Service
        implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private MusicDao musicDao;
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private NotificationBar notificationBar;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final EntityDataStore<Persistable> dataStore = ((PlayerApplication) getApplication()).getData();
        this.musicDao = new MusicDao(dataStore);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        final AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA);
        final AudioAttributes attributes = builder.build();
        this.mediaPlayer.setAudioAttributes(attributes);

        this.notificationBar = new NotificationBar(getApplicationContext().getPackageName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
            final Song song = this.musicDao.findSong(songId);
            setCurrentSong(song);
        }
        return START_STICKY;
    }

    private void setCurrentSong(Song song) {
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

            updateNotificationBar();
        } catch (IOException e) {
            Log.e(getClass().getName(), "Can't play song: " + e.getMessage());
        }
    }

    private void updateNotificationBar() {
        this.notificationBar.setSong(this.currentSong);

        /*Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setAutoCancel(false)
                .setOngoing(true)
                //.setContentIntent(pendingIntent)
                .setContent(this.notificationBar);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationType.DEFAULT_START.getCode(), builder.build());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onDestroy() {
        this.mediaPlayer.release();
        this.mediaPlayer = null;
        this.musicDao = null;
        super.onDestroy();
    }
}
