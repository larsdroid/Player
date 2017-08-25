package org.willemsens.player.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.notification.NotificationBarBig;
import org.willemsens.player.notification.NotificationBarSmall;
import org.willemsens.player.notification.NotificationType;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.view.main.MainActivity;

import java.io.IOException;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class MusicPlayingService extends Service
        implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private MusicDao musicDao;
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private PlayMode playMode;
    private PlayStatus playStatus;
    private NotificationBarSmall notificationBarSmall;
    private NotificationBarBig notificationBarBig;
    private NotificationManager notificationManager;

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
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

        this.playMode = PlayMode.NO_REPEAT;
        this.playStatus = PlayStatus.STOPPED;

        final AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA);
        final AudioAttributes attributes = builder.build();
        this.mediaPlayer.setAudioAttributes(attributes);

        this.notificationBarSmall = new NotificationBarSmall(getApplicationContext().getPackageName());
        this.notificationBarBig = new NotificationBarBig(getApplicationContext().getPackageName());

        initNotificationManager();
    }

    private void initNotificationManager() {
        this.notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    NotificationType.MUSIC_PLAYING.getChannel(),
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.channel_description));
            this.notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
            final Song song = this.musicDao.findSong(songId);
            setCurrentSong(song, PlayMode.valueOf(intent.getStringExtra(getString(R.string.key_play_mode))));
        }
        return START_STICKY;
    }

    private void setCurrentSong(Song song) {
        setCurrentSong(song, this.playMode);
    }

    private void setCurrentSong(Song song, PlayMode playMode) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            this.playStatus = PlayStatus.STOPPED;
        }
        if (!song.equals(currentSong)) {
            mediaPlayer.reset();
        }

        this.currentSong = song;
        this.playMode = playMode;

        try {
            mediaPlayer.setDataSource(song.getFile());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(getClass().getName(), "Can't play song: " + e.getMessage());
        }
    }

    private Notification createNotification() {
        this.notificationBarSmall.setSong(this.currentSong);
        this.notificationBarBig.setSong(this.currentSong);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext(), NotificationType.MUSIC_PLAYING.getChannel())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setAutoCancel(false)
                .setOngoing(this.playStatus == PlayStatus.PLAYING)
                .setContentIntent(pendingIntent)
                .setCustomBigContentView(this.notificationBarBig)
                .setCustomContentView(this.notificationBarSmall);

        return builder.build();
    }

    private void showUpdateNotification() {
        this.notificationManager.notify(NotificationType.MUSIC_PLAYING.getCode(), createNotification());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        this.mediaPlayer.start();
        this.playStatus = PlayStatus.PLAYING;
        showUpdateNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.playMode == PlayMode.REPEAT_ONE) {
            setCurrentSong(this.currentSong);
        } else {
            Song nextSong = this.musicDao.findNextSong(this.currentSong);
            if (nextSong == null) {
                if (this.playMode == PlayMode.REPEAT_ALL) {
                    nextSong = this.musicDao.findFirstSong(this.currentSong.getAlbum());
                    setCurrentSong(nextSong);
                } else {
                    // TODO: set song null? set song to first song from album AND stopped?
                    // TODO: dismiss notification?
                    // TODO: something else?
                    this.playStatus = PlayStatus.STOPPED;
                    showUpdateNotification();
                }
            } else {
                setCurrentSong(nextSong);
            }
        }
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
        this.playMode = PlayMode.NO_REPEAT;
        this.playStatus = PlayStatus.STOPPED;
        super.onDestroy();
    }
}
