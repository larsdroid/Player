package org.willemsens.player.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.willemsens.player.R;
import org.willemsens.player.playback.notification.NotificationBarBig;
import org.willemsens.player.playback.notification.NotificationBarSmall;
import org.willemsens.player.playback.notification.NotificationType;
import org.willemsens.player.view.main.MainActivity;

import static org.willemsens.player.playback.PlayStatus.PLAYING;
import static org.willemsens.player.playback.PlayStatus.STOPPED;
import static org.willemsens.player.playback.PlayerCommand.DISMISS;
import static org.willemsens.player.playback.PlayerCommand.NEXT;
import static org.willemsens.player.playback.PlayerCommand.PREVIOUS;
import static org.willemsens.player.playback.PlayerCommand.STOP_PLAY_PAUSE;

public class PlayBackService extends Service implements Player.OnUpdateListener {
    private NotificationBarSmall notificationBarSmall;
    private NotificationBarBig notificationBarBig;
    private NotificationManager notificationManager;
    private Player player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.player = new Player(getApplication(), this, this);

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
                this,
                PREVIOUS.getRequestCode(),
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
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_description));
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.enableLights(false);
            this.notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String intentAction = intent.getAction();
        if (intentAction != null) {
            if (intentAction.equals(getString(R.string.key_action_dismiss))) {
                stopSelf();
            } else if (intentAction.equals(getString(R.string.key_action_set_song_id))) {
                final long songId = intent.getLongExtra(getString(R.string.key_playback_song_id), -1);
                PlayerCommand playerCommand = null;
                if (intent.hasExtra(getString(R.string.key_player_command))) {
                    playerCommand = PlayerCommand.valueOf(intent.getStringExtra(getString(R.string.key_player_command)));
                }
                this.player.setSong(songId, playerCommand);
            } else if (intentAction.equals(getString(R.string.key_action_player_command))) {
                final PlayerCommand playerCommand = PlayerCommand.valueOf(intent.getStringExtra(getString(R.string.key_player_command)));
                this.player.processCommand(playerCommand);
            } else if (intentAction.equals(getString(R.string.key_action_set_play_mode))) {
                PlayMode playMode = PlayMode.valueOf(intent.getStringExtra(getString(R.string.key_play_mode)));
                // TODO
            } else {
                Log.e(getClass().getName(), "Invalid intent received in PlayBackService::onStartCommand");
            }
        }
        return START_NOT_STICKY;
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent deleteIntent = PendingIntent.getService(
                this,
                DISMISS.getRequestCode(),
                new PlayBackIntentBuilder(this).dismiss().build(),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext(), NotificationType.MUSIC_PLAYING.getChannel())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setAutoCancel(false)
                .setOngoing(this.player.getPlayStatus() == PLAYING)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deleteIntent)
                .setCustomBigContentView(this.notificationBarBig)
                .setCustomContentView(this.notificationBarSmall);

        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (this.player.getPlayStatus() != PLAYING && android.os.Build.VERSION.SDK_INT >= 24) {
            stopForeground(0);
        }

        if (this.player.getPlayStatus() == STOPPED) {
            this.notificationManager.cancel(NotificationType.MUSIC_PLAYING.getCode());
            stopSelf();
        } else {
            this.notificationBarSmall.update(this.player.getSong(), this.player.getPlayStatus());
            this.notificationBarBig.update(this.player.getSong(), this.player.getPlayStatus());
            if (this.player.getPlayStatus() == PLAYING) {
                startForeground(NotificationType.MUSIC_PLAYING.getCode(), createNotification());
            } else {
                this.notificationManager.notify(NotificationType.MUSIC_PLAYING.getCode(), createNotification());
            }
        }

        Intent broadcast = new Intent(getString(R.string.key_player_status_update));
        sendBroadcast(broadcast);
    }

    @Override
    public void onDestroy() {
        this.player.release();
        super.onDestroy();
    }
}
