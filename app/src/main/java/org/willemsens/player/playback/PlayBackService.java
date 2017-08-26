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
import android.support.v4.content.LocalBroadcastManager;
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

import static org.willemsens.player.playback.PlayerCommand.NEXT;
import static org.willemsens.player.playback.PlayerCommand.PREVIOUS;
import static org.willemsens.player.playback.PlayerCommand.STOP_PLAY_PAUSE;
import static org.willemsens.player.playback.PlayStatus.PAUSED;
import static org.willemsens.player.playback.PlayStatus.PLAYING;
import static org.willemsens.player.playback.PlayStatus.STOPPED;

public class PlayBackService extends Service
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

        final PlayBackIntentBuilder intentBuilder = new PlayBackIntentBuilder(this);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                STOP_PLAY_PAUSE.getRequestCode(),
                intentBuilder.setPlayerCommand(STOP_PLAY_PAUSE).build(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificationBarSmall.setOnClickPendingIntent(R.id.button_play_pause_stop, pendingIntent);
        this.notificationBarBig.setOnClickPendingIntent(R.id.button_play_pause_stop, pendingIntent);

        pendingIntent = PendingIntent.getService(
                this, PREVIOUS.getRequestCode(),
                intentBuilder.setPlayerCommand(PREVIOUS).build(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificationBarSmall.setOnClickPendingIntent(R.id.button_previous, pendingIntent);
        this.notificationBarBig.setOnClickPendingIntent(R.id.button_previous, pendingIntent);

        pendingIntent = PendingIntent.getService(
                this,
                NEXT.getRequestCode(),
                intentBuilder.setPlayerCommand(NEXT).build(),
                PendingIntent.FLAG_UPDATE_CURRENT);
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

                if (intent.hasExtra(getString(R.string.key_play_command))) {
                    final PlayerCommand playerCommand = PlayerCommand.valueOf(intent.getStringExtra(getString(R.string.key_play_command)));
                    switch (playerCommand) {
                        case PLAY:
                            this.playStatus = PLAYING;
                    }
                }

                setCurrentSong(song);
            } else if (intent.hasExtra(getString(R.string.key_play_command))) {
                final PlayerCommand playerCommand = PlayerCommand.valueOf(intent.getStringExtra(getString(R.string.key_play_command)));
                if (playerCommand == PREVIOUS || playerCommand == NEXT) {
                    Song newSong;
                    if (playerCommand == PREVIOUS) {
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
                } else if (playerCommand == STOP_PLAY_PAUSE) {
                    if (this.playStatus == STOPPED && this.currentSong != null) {
                        this.playStatus = PLAYING;
                        setCurrentSong(this.currentSong); // TODO: not optimal to reload this song, I guess...
                    } else if (this.playStatus == PAUSED) {
                        this.playStatus = PLAYING;
                        this.mediaPlayer.start();
                        broadcastAndNotification();
                    } else if (this.playStatus == PLAYING) {
                        this.playStatus = PAUSED;
                        this.mediaPlayer.pause();
                        broadcastAndNotification();
                    }
                } else {
                    Log.e(getClass().getName(), "Invalid PlayerCommand received in PlayBackService::onStartCommand");
                }
            } else if (intent.hasExtra(getString(R.string.key_play_command))) {
                PlayMode playMode = PlayMode.valueOf(intent.getStringExtra(getString(R.string.key_play_mode)));
                // TODO
            } else {
                Log.e(getClass().getName(), "Invalid intent received in PlayBackService::onStartCommand");
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

    private void broadcastAndNotification() {
        this.notificationBarSmall.update(this.currentSong, this.playStatus);
        this.notificationBarBig.update(this.currentSong, this.playStatus);
        this.notificationManager.notify(NotificationType.MUSIC_PLAYING.getCode(), createNotification());

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent broadcast = new Intent(getString(R.string.key_player_status_changed));
        broadcast.putExtra(getString(R.string.key_song_id), this.currentSong.getId());
        broadcast.putExtra(getString(R.string.key_play_status), this.playStatus.name());
        lbm.sendBroadcast(broadcast);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (this.playStatus == PLAYING) {
            this.mediaPlayer.start();
        }
        broadcastAndNotification();
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
                    broadcastAndNotification();
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
