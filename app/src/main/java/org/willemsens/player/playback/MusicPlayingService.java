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
import org.willemsens.player.playback.notification.NotificationBarBig;
import org.willemsens.player.playback.notification.NotificationBarSmall;
import org.willemsens.player.playback.notification.NotificationType;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.view.main.MainActivity;

import java.io.IOException;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

import static org.willemsens.player.playback.PlayCommand.NEXT;
import static org.willemsens.player.playback.PlayCommand.PREVIOUS;
import static org.willemsens.player.playback.PlayCommand.STOP_PLAY_PAUSE;
import static org.willemsens.player.playback.PlayStatus.PAUSED;
import static org.willemsens.player.playback.PlayStatus.PLAYING;
import static org.willemsens.player.playback.PlayStatus.STOPPED;

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

        initNotificationBars();
        initNotificationManager();
    }

    private void initNotificationBars() {
        this.notificationBarSmall = new NotificationBarSmall(getApplicationContext().getPackageName());
        this.notificationBarBig = new NotificationBarBig(getApplicationContext().getPackageName());

        Intent intent = new Intent(this, MusicPlayingService.class);
        intent.putExtra(getString(R.string.key_play_command), STOP_PLAY_PAUSE.name());
        PendingIntent pendingIntent = PendingIntent.getService(this, STOP_PLAY_PAUSE.getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificationBarSmall.setOnClickPendingIntent(R.id.button_play_pause_stop, pendingIntent);
        this.notificationBarBig.setOnClickPendingIntent(R.id.button_play_pause_stop, pendingIntent);

        intent = new Intent(this, MusicPlayingService.class);
        intent.putExtra(getString(R.string.key_play_command), PREVIOUS.name());
        pendingIntent = PendingIntent.getService(this, PREVIOUS.getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificationBarSmall.setOnClickPendingIntent(R.id.button_previous, pendingIntent);
        this.notificationBarBig.setOnClickPendingIntent(R.id.button_previous, pendingIntent);

        intent = new Intent(this, MusicPlayingService.class);
        intent.putExtra(getString(R.string.key_play_command), NEXT.name());
        pendingIntent = PendingIntent.getService(this, NEXT.getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificationBarSmall.setOnClickPendingIntent(R.id.button_next, pendingIntent);
        this.notificationBarBig.setOnClickPendingIntent(R.id.button_next, pendingIntent);
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
            if (intent.hasExtra(getString(R.string.key_song_id))) {
                final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
                final Song song = this.musicDao.findSong(songId);
                this.playStatus = PLAYING;
                setCurrentSong(song);
            } else if (intent.hasExtra(getString(R.string.key_play_command))) {
                final PlayCommand playCommand = PlayCommand.valueOf(intent.getStringExtra(getString(R.string.key_play_command)));
                if (playCommand == PREVIOUS || playCommand == NEXT) {
                    Song newSong;
                    if (playCommand == PREVIOUS) {
                        newSong = this.musicDao.findPreviousSong(this.currentSong);
                        if (newSong == null) {
                            newSong = this.musicDao.findLastSong(this.currentSong.getAlbum());
                        }
                    } else {
                        newSong = this.musicDao.findNextSong(this.currentSong);
                        if (newSong == null) {
                            newSong = this.musicDao.findFirstSong(this.currentSong.getAlbum());
                        }
                    }

                    setCurrentSong(newSong);
                } else if (playCommand == STOP_PLAY_PAUSE) {
                    if (this.playStatus == STOPPED && this.currentSong != null) {
                        this.playStatus = PLAYING;
                        setCurrentSong(this.currentSong); // TODO: not optimal to reload this song, I guess...
                    } else if (this.playStatus == PAUSED) {
                        this.playStatus = PLAYING;
                        this.mediaPlayer.start();
                    } else if (this.playStatus == PLAYING) {
                        this.playStatus = PAUSED;
                        this.mediaPlayer.pause();
                    }
                } else {
                    Log.e(getClass().getName(), "Invalid PlayCommand received in MusicPlayingService::onStartCommand");
                }
            } else if (intent.hasExtra(getString(R.string.key_play_command))) {
                PlayMode playMode = PlayMode.valueOf(intent.getStringExtra(getString(R.string.key_play_mode)));
                // TODO
            } else {
                Log.e(getClass().getName(), "Invalid intent received in MusicPlayingService::onStartCommand");
            }
        }
        return START_STICKY;
    }

    private void setCurrentSong(Song song) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        this.currentSong = song;

        try {
            mediaPlayer.setDataSource(song.getFile());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(getClass().getName(), "Can't play song: " + e.getMessage());
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext(), NotificationType.MUSIC_PLAYING.getChannel())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setAutoCancel(false)
                .setOngoing(this.playStatus == PLAYING)
                .setContentIntent(pendingIntent)
                .setCustomBigContentView(this.notificationBarBig)
                .setCustomContentView(this.notificationBarSmall);

        return builder.build();
    }

    private void showUpdateNotification() {
        this.notificationBarSmall.update(this.currentSong, this.playStatus);
        this.notificationBarBig.update(this.currentSong, this.playStatus);
        this.notificationManager.notify(NotificationType.MUSIC_PLAYING.getCode(), createNotification());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (this.playStatus == PLAYING) {
            this.mediaPlayer.start();
        }
        showUpdateNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.playMode == PlayMode.REPEAT_ONE && this.playStatus == PLAYING) {
            // TODO: the looping functionality of MediaPlayer can be used
            this.mediaPlayer.start();
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
