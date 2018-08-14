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
import org.willemsens.player.exceptions.PlayerException;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Song;
import org.willemsens.player.playback.eventbus.AlbumProgressUpdatedMessage;
import org.willemsens.player.playback.eventbus.CurrentPlayStatusMessage;
import org.willemsens.player.playback.eventbus.CurrentAlbumOrSongMessage;
import org.willemsens.player.playback.eventbus.PlayBackEventBus;

import java.io.File;

import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static org.willemsens.player.playback.PlayStatus.PAUSED;
import static org.willemsens.player.playback.PlayStatus.PLAYING;
import static org.willemsens.player.playback.PlayStatus.STOPPED;
import static org.willemsens.player.playback.PlayerCommand.PLAY;
import static org.willemsens.player.playback.PlayerCommand.PREVIOUS;

public class Player extends com.google.android.exoplayer2.Player.DefaultEventListener {
    private final Context context;
    private final MusicDao musicDao;
    private final SimpleExoPlayer exoPlayer;
    private final OnUpdateListener onUpdateListener;
    private final DataSource.Factory dataSourceFactory;

    Player(Context context, OnUpdateListener onUpdateListener) {
        this.context = context;
        this.onUpdateListener = onUpdateListener;

        this.musicDao = AppDatabase.getAppDatabase(context).musicDao();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        this.exoPlayer.addListener(this);
        this.dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_name)), null);

        final Album currentAlbum = this.musicDao.getCurrentAlbum();
        if (currentAlbum != null) {
            this.setCurrentAlbum(currentAlbum);
        }
    }

    private void setCurrentAlbum(Album album) {
        int track = album.currentTrack == null ? 1 : album.currentTrack;
        final Song song = this.musicDao.findSong(album.id, track);
        if (song != null) {
            setCurrentSong(album, song, false, false);
        } else {
            throw new PlayerException("Couldn't find track " + track + " of album " + album.name
                    + " (year " + album.yearReleased + "). [album.currentTrack = " + album.currentTrack + "]");
        }

    }

    private void startSong(Album album, Song song) {
        setCurrentSong(album, song, true, true);
    }

    private void setCurrentSong(Album album, Song song, boolean notifyListeners, boolean resetToStartOfSong) {
        if (song.albumId != album.id) {
            throw new RuntimeException("Trying to set a song of a different album.");
        }
        album.currentTrack = song.track;
        if (resetToStartOfSong) {
            album.currentMillisInTrack = 0;
        }
        updateAppStateCurrentAlbum(album, song);
        updateAppStateAlbumProgress(album);

        MediaSource musicSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(new File(song.file)));
        this.exoPlayer.prepare(musicSource);

        if (album.currentMillisInTrack != null && album.currentMillisInTrack != 0) {
            this.exoPlayer.seekTo(album.currentMillisInTrack);
        }

        this.exoPlayer.setPlayWhenReady(getPlayStatus() == PLAYING);
        if (notifyListeners) {
            this.onUpdateListener.onUpdate();
        }
    }

    PlayStatus getPlayStatus() {
        return this.musicDao.findCurrentPlayStatus();
    }

    Song getSong() {
        final Album album = this.musicDao.getCurrentAlbum();
        if (album != null) {
            return this.musicDao.findSong(album.id, album.currentTrack == null ? 1 : album.currentTrack);
        } else {
            return null;
        }
    }

    private void updateAppStatePlayStatus(PlayStatus playStatus) {
        this.musicDao.setCurrentPlayStatus(playStatus);
        PlayBackEventBus.postAcrossProcess(new CurrentPlayStatusMessage(playStatus), this.context);
    }

    private void updateAppStateCurrentAlbum(Album album, Song song) {
        this.musicDao.setCurrentAlbum(album);
        PlayBackEventBus.postAcrossProcess(new CurrentAlbumOrSongMessage(album.id, song.id), this.context);
    }

    private void updateAppStateAlbumProgress(Album album) {
        this.musicDao.updateAlbum(album);
        PlayBackEventBus.postAcrossProcess(
                new AlbumProgressUpdatedMessage(
                        album.id,
                        album.currentTrack,
                        album.currentMillisInTrack,
                        album.playCount),
                this.context);
    }

    void startSong(final long songId, final PlayerCommand playerCommand) {
        final Song song = this.musicDao.findSong(songId);
        if (playerCommand == PLAY) {
            this.updateAppStatePlayStatus(PLAYING);
        } else {
            Log.e(getClass().getName(), "Invalid PlayerCommand received in Player::startSong");
        }
        final Album album = this.musicDao.findAlbum(song.albumId);
        startSong(album, song);
    }

    void startOrContinueAlbum(final long albumId, final PlayerCommand playerCommand) {
        if (playerCommand == PLAY) {
            this.updateAppStatePlayStatus(PLAYING);
        } else {
            Log.e(getClass().getName(), "Invalid PlayerCommand received in Player::startSong");
        }
        final Album album = this.musicDao.findAlbum(albumId);
        final Song song;
        if (album.currentTrack != null) {
            song = this.musicDao.findSong(albumId, album.currentTrack);
        } else {
            song = this.musicDao.findFirstSong(albumId);
        }
        setCurrentSong(album, song, true, false);
    }

    void processCommand(final PlayerCommand playerCommand) {
        switch (playerCommand) {
            case PREVIOUS:
            case NEXT:
                final Album currentAlbum = this.musicDao.getCurrentAlbum();
                Song newSong;
                if (playerCommand == PREVIOUS) {
                    newSong = this.musicDao.findPreviousSong(currentAlbum.id, currentAlbum.currentTrack == null ? 1 : currentAlbum.currentTrack);
                    if (newSong == null) {
                        newSong = this.musicDao.findLastSong(currentAlbum.id);
                    }
                } else {
                    newSong = this.musicDao.findNextSong(currentAlbum.id, currentAlbum.currentTrack == null ? 1 : currentAlbum.currentTrack);
                    if (newSong == null) {
                        newSong = this.musicDao.findFirstSong(currentAlbum.id);
                    }
                }

                startSong(currentAlbum, newSong);
                break;
            case STOP_PLAY_PAUSE:
                final Album currentAlb = this.musicDao.getCurrentAlbum();
                if (this.musicDao.findCurrentPlayStatus() == STOPPED && currentAlb != null) {
                    this.updateAppStatePlayStatus(PLAYING);
                    final Song song = this.musicDao.findSong(currentAlb.id, currentAlb.currentTrack == null ? 1 : currentAlb.currentTrack);
                    startSong(currentAlb, song); // Not optimal to reload this song, I guess...
                } else if (this.musicDao.findCurrentPlayStatus() == PAUSED) {
                    this.updateAppStatePlayStatus(PLAYING);
                    this.exoPlayer.setPlayWhenReady(true);
                    this.onUpdateListener.onUpdate();
                } else if (this.musicDao.findCurrentPlayStatus() == PLAYING) {
                    this.updateAppStatePlayStatus(PAUSED);
                    this.exoPlayer.setPlayWhenReady(false);
                    this.onUpdateListener.onUpdate();
                }
                break;
            case PAUSE:
                if (this.musicDao.findCurrentPlayStatus() == PLAYING) {
                    this.updateAppStatePlayStatus(PAUSED);
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
                final Album currentAlbum = this.musicDao.getCurrentAlbum();
                Song nextSong = this.musicDao.findNextSong(currentAlbum.id, currentAlbum.currentTrack == null ? 1 : currentAlbum.currentTrack);
                if (nextSong == null) {
                    currentAlbum.playCount++;
                    if (this.musicDao.getCurrentPlayMode() == PlayMode.REPEAT_ALL) {
                        nextSong = this.musicDao.findFirstSong(currentAlbum.id);
                        startSong(currentAlbum, nextSong);
                    } else {
                        this.updateAppStatePlayStatus(STOPPED);

                        currentAlbum.currentTrack = null;
                        currentAlbum.currentMillisInTrack = null;

                        updateAppStateAlbumProgress(currentAlbum);
                    }
                } else {
                    startSong(currentAlbum, nextSong);
                }
            }
        }
    }

    void persistTrackMillis() {
        final Album album = this.musicDao.getCurrentAlbum();
        if (album != null) {
            album.currentMillisInTrack = (int) this.exoPlayer.getCurrentPosition();
            updateAppStateAlbumProgress(album);
        }
    }

    void release() {
        if (getPlayStatus() == PLAYING) {
            this.updateAppStatePlayStatus(PAUSED);
        }
        persistTrackMillis();
        this.exoPlayer.release();
    }

    interface OnUpdateListener {
        void onUpdate();
    }
}
