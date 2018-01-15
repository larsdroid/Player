package org.willemsens.player.playback;

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.PlayerApplication;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;

import java.io.IOException;

import static org.willemsens.player.playback.PlayStatus.PAUSED;
import static org.willemsens.player.playback.PlayStatus.PLAYING;
import static org.willemsens.player.playback.PlayStatus.STOPPED;
import static org.willemsens.player.playback.PlayerCommand.PLAY;
import static org.willemsens.player.playback.PlayerCommand.PREVIOUS;

public class Player implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private final MusicDao musicDao;
    private final MediaPlayer mediaPlayer;
    private final OnUpdateListener onUpdateListener;
    private int millis;

    Player(Application application, Context context, OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;

        final EntityDataStore<Persistable> dataStore = ((PlayerApplication) application).getData();
        this.musicDao = new MusicDao(dataStore, context);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

        final AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA);
        final AudioAttributes attributes = builder.build();
        this.mediaPlayer.setAudioAttributes(attributes);

        final Song currentSong = this.musicDao.getCurrentSong();
        if (currentSong != null) {
            this.setCurrentSong(currentSong);
            this.millis = this.musicDao.getCurrentMillis();
        }
    }

    private void setCurrentSong(Song song) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        this.musicDao.setCurrentSong(song);

        try {
            mediaPlayer.setDataSource(song.getFile());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(getClass().getName(), "Can't play song: " + e.getMessage());
        }
    }

    PlayStatus getPlayStatus() {
        return this.musicDao.getCurrentPlayStatus();
    }

    Song getSong() {
        return this.musicDao.getCurrentSong();
    }

    void setSong(final long songId, final PlayerCommand playerCommand) {
        final Song song = this.musicDao.findSong(songId);
        if (playerCommand == PLAY) {
            this.musicDao.setCurrentPlayStatus(PLAYING);
        } else {
            Log.e(getClass().getName(), "Invalid PlayerCommand received in Player::setSong");
        }
        setCurrentSong(song);
    }

    void processCommand(final PlayerCommand playerCommand) {
        switch (playerCommand) {
            case PREVIOUS:
            case NEXT:
                Song newSong;
                if (playerCommand == PREVIOUS) {
                    newSong = this.musicDao.findPreviousSong(this.musicDao.getCurrentSong());
                    if (newSong == null) {
                        newSong = this.musicDao.findLastSong(this.musicDao.getCurrentSong().getAlbum());
                    }
                } else {
                    newSong = this.musicDao.findNextSong(this.musicDao.getCurrentSong());
                    if (newSong == null) {
                        newSong = this.musicDao.findFirstSong(this.musicDao.getCurrentSong().getAlbum());
                    }
                }

                setCurrentSong(newSong);
                break;
            case STOP_PLAY_PAUSE:
                if (this.musicDao.getCurrentPlayStatus() == STOPPED && this.musicDao.getCurrentSong() != null) {
                    this.musicDao.setCurrentPlayStatus(PLAYING);
                    setCurrentSong(this.musicDao.getCurrentSong()); // TODO: not optimal to reload this song, I guess...
                } else if (this.musicDao.getCurrentPlayStatus() == PAUSED) {
                    this.musicDao.setCurrentPlayStatus(PLAYING);
                    this.mediaPlayer.start();
                    this.onUpdateListener.onUpdate();
                } else if (this.musicDao.getCurrentPlayStatus() == PLAYING) {
                    this.musicDao.setCurrentPlayStatus(PAUSED);
                    this.mediaPlayer.pause();
                    this.onUpdateListener.onUpdate();
                }
                break;
            case PAUSE:
                if (this.musicDao.getCurrentPlayStatus() == PLAYING) {
                    this.musicDao.setCurrentPlayStatus(PAUSED);
                    this.mediaPlayer.pause();
                    this.onUpdateListener.onUpdate();
                }
                break;
            default:
                Log.e(getClass().getName(), "Invalid PlayerCommand received in Player::processCommand");
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (this.millis != 0) {
            this.mediaPlayer.seekTo(this.millis);
        }

        if (getPlayStatus() == PLAYING) {
            this.mediaPlayer.start();
        }

        this.onUpdateListener.onUpdate();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.musicDao.getCurrentPlayMode() == PlayMode.REPEAT_ONE && getPlayStatus() == PLAYING) {
            // TODO: the looping functionality of MediaPlayer can be used
            //       ... but it probably shouldn't since we want to track the number of plays.
            this.mediaPlayer.start();
        } else {
            Song nextSong = this.musicDao.findNextSong(this.musicDao.getCurrentSong());
            if (nextSong == null) {
                if (this.musicDao.getCurrentPlayMode() == PlayMode.REPEAT_ALL) {
                    nextSong = this.musicDao.findFirstSong(this.musicDao.getCurrentSong().getAlbum());
                    setCurrentSong(nextSong);
                } else {
                    this.musicDao.setCurrentPlayStatus(STOPPED);
                    this.onUpdateListener.onUpdate();
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

    void release() {
        if (getPlayStatus() == PLAYING) {
            this.musicDao.setCurrentPlayStatus(PAUSED);
        }
        this.musicDao.setCurrentMillis(this.mediaPlayer.getCurrentPosition());
        this.mediaPlayer.release();
    }

    interface OnUpdateListener {
        void onUpdate();
    }
}
