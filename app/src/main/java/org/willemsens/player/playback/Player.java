package org.willemsens.player.playback;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import org.willemsens.player.R;
import org.willemsens.player.persistence.entities.Song;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;

import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static org.willemsens.player.playback.PlayStatus.PAUSED;
import static org.willemsens.player.playback.PlayStatus.PLAYING;
import static org.willemsens.player.playback.PlayStatus.STOPPED;
import static org.willemsens.player.playback.PlayerCommand.PLAY;
import static org.willemsens.player.playback.PlayerCommand.PREVIOUS;

public class Player extends com.google.android.exoplayer2.Player.DefaultEventListener {
    private final MusicDao musicDao;
    private final SimpleExoPlayer exoPlayer;
    private final OnUpdateListener onUpdateListener;
    private final DataSource.Factory dataSourceFactory;
    private long millis;

    Player(Context context, OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;

        this.musicDao = AppDatabase.getAppDatabase(context).musicDao();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        this.dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_name)), null);

        final Song currentSong = this.musicDao.getCurrentSong_NonLive();
        if (currentSong != null) {
            this.setCurrentSong(currentSong, false);
            this.millis = this.musicDao.getCurrentMillis();
        }
    }

    private void setCurrentSong(Song song) {
        setCurrentSong(song, true);
    }

    private void setCurrentSong(Song song, boolean notifyListeners) {
        this.musicDao.setCurrentSong(song);

        MediaSource musicSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(new File(song.file)));
        this.exoPlayer.prepare(musicSource);

        /*
        TODO: seek to position
        if (this.millis != 0) {
            this.mediaPlayer.seekTo(this.millis);
        }*/

        this.exoPlayer.setPlayWhenReady(getPlayStatus() == PLAYING);
        if (notifyListeners) {
            this.onUpdateListener.onUpdate();
        }
    }

    PlayStatus getPlayStatus() {
        return this.musicDao.getCurrentPlayStatus();
    }

    Song getSong() {
        return this.musicDao.getCurrentSong_NonLive();
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
                final Song currentSong = this.musicDao.getCurrentSong_NonLive();
                Song newSong;
                if (playerCommand == PREVIOUS) {
                    newSong = this.musicDao.findPreviousSong(currentSong.albumId, currentSong.track);
                    if (newSong == null) {
                        newSong = this.musicDao.findLastSong(currentSong.albumId);
                    }
                } else {
                    newSong = this.musicDao.findNextSong(currentSong.albumId, currentSong.track);
                    if (newSong == null) {
                        newSong = this.musicDao.findFirstSong(currentSong.albumId);
                    }
                }

                setCurrentSong(newSong);
                break;
            case STOP_PLAY_PAUSE:
                if (this.musicDao.getCurrentPlayStatus() == STOPPED && this.musicDao.getCurrentSong_NonLive() != null) {
                    this.musicDao.setCurrentPlayStatus(PLAYING);
                    setCurrentSong(this.musicDao.getCurrentSong_NonLive()); // TODO: not optimal to reload this song, I guess...
                } else if (this.musicDao.getCurrentPlayStatus() == PAUSED) {
                    this.musicDao.setCurrentPlayStatus(PLAYING);
                    this.exoPlayer.setPlayWhenReady(true);
                    this.onUpdateListener.onUpdate();
                } else if (this.musicDao.getCurrentPlayStatus() == PLAYING) {
                    this.musicDao.setCurrentPlayStatus(PAUSED);
                    this.exoPlayer.setPlayWhenReady(false);
                    this.onUpdateListener.onUpdate();
                }
                break;
            case PAUSE:
                if (this.musicDao.getCurrentPlayStatus() == PLAYING) {
                    this.musicDao.setCurrentPlayStatus(PAUSED);
                    this.exoPlayer.setPlayWhenReady(false);
                    this.onUpdateListener.onUpdate();
                }
                break;
            default:
                Log.e(getClass().getName(), "Invalid PlayerCommand received in Player::processCommand");
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == STATE_ENDED) {
            if (this.musicDao.getCurrentPlayMode() == PlayMode.REPEAT_ONE && getPlayStatus() == PLAYING) {
                // TODO: the looping functionality of MediaPlayer can be used
                //       ... but it probably shouldn't since we want to track the number of plays.
                this.exoPlayer.setPlayWhenReady(true);
            } else {
                final Song currentSong = this.musicDao.getCurrentSong_NonLive();
                Song nextSong = this.musicDao.findNextSong(currentSong.albumId, currentSong.track);
                if (nextSong == null) {
                    if (this.musicDao.getCurrentPlayMode() == PlayMode.REPEAT_ALL) {
                        nextSong = this.musicDao.findFirstSong(currentSong.albumId);
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
    }

    void release() {
        if (getPlayStatus() == PLAYING) {
            this.musicDao.setCurrentPlayStatus(PAUSED);
        }
        this.musicDao.setCurrentMillis(this.exoPlayer.getCurrentPosition());
        this.exoPlayer.release();
    }

    interface OnUpdateListener {
        void onUpdate();
    }
}
