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
import org.willemsens.player.persistence.entities.Album;
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

    Player(Context context, OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;

        this.musicDao = AppDatabase.getAppDatabase(context).musicDao();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        this.dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_name)), null);

        final Album currentAlbum = this.musicDao.getCurrentAlbum();
        if (currentAlbum != null) {
            this.setCurrentAlbum(currentAlbum);
        }
    }

    private void setCurrentAlbum(Album album) {
        final Song song = this.musicDao.findSong(album.id, album.currentTrack == null ? 1 : album.currentTrack);
        setCurrentSong(album, song, false, false);
    }

    private void startSong(Album album, Song song) {
        setCurrentSong(album, song, true, true);
    }

    private void setCurrentSong(Album album, Song song, boolean notifyListeners, boolean resetToStartOfSong) {
        if (song.albumId != album.id) {
            Log.e(getClass().getName(), "Trying to set a song of a different album.");
            throw new RuntimeException("Trying to set a song of a different album.");
        }
        album.currentTrack = song.track;
        if (resetToStartOfSong) {
            album.currentMillisInTrack = 0;
        }
        this.musicDao.setCurrentAlbum(album);
        this.musicDao.updateAlbum(album);

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
        return this.musicDao.getCurrentPlayStatus_NON_Live();
    }

    Song getSong() {
        final Album album = this.musicDao.getCurrentAlbum();
        if (album != null) {
            return this.musicDao.findSong(album.id, album.currentTrack == null ? 1 : album.currentTrack);
        } else {
            return null;
        }
    }

    void startSong(final long songId, final PlayerCommand playerCommand) {
        final Song song = this.musicDao.findSong(songId);
        if (playerCommand == PLAY) {
            this.musicDao.setCurrentPlayStatus(PLAYING);
        } else {
            Log.e(getClass().getName(), "Invalid PlayerCommand received in Player::startSong");
        }
        final Album album = this.musicDao.findAlbum(song.albumId);
        startSong(album, song);
    }

    void startOrContinueAlbum(final long albumId, final PlayerCommand playerCommand) {
        if (playerCommand == PLAY) {
            this.musicDao.setCurrentPlayStatus(PLAYING);
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
                if (this.musicDao.getCurrentPlayStatus_NON_Live() == STOPPED && currentAlb != null) {
                    this.musicDao.setCurrentPlayStatus(PLAYING);
                    final Song song = this.musicDao.findSong(currentAlb.id, currentAlb.currentTrack == null ? 1 : currentAlb.currentTrack);
                    startSong(currentAlb, song); // Not optimal to reload this song, I guess...
                } else if (this.musicDao.getCurrentPlayStatus_NON_Live() == PAUSED) {
                    this.musicDao.setCurrentPlayStatus(PLAYING);
                    this.exoPlayer.setPlayWhenReady(true);
                    this.onUpdateListener.onUpdate();
                } else if (this.musicDao.getCurrentPlayStatus_NON_Live() == PLAYING) {
                    this.musicDao.setCurrentPlayStatus(PAUSED);
                    this.exoPlayer.setPlayWhenReady(false);
                    this.onUpdateListener.onUpdate();
                }
                break;
            case PAUSE:
                if (this.musicDao.getCurrentPlayStatus_NON_Live() == PLAYING) {
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
                final Album currentAlbum = this.musicDao.getCurrentAlbum();
                Song nextSong = this.musicDao.findNextSong(currentAlbum.id, currentAlbum.currentTrack == null ? 1 : currentAlbum.currentTrack);
                if (nextSong == null) {
                    if (this.musicDao.getCurrentPlayMode() == PlayMode.REPEAT_ALL) {
                        nextSong = this.musicDao.findFirstSong(currentAlbum.id);
                        startSong(currentAlbum, nextSong);
                    } else {
                        this.musicDao.setCurrentPlayStatus(STOPPED);
                        this.onUpdateListener.onUpdate();
                    }
                } else {
                    startSong(currentAlbum, nextSong);
                }
            }
        }
    }

    void persistTrackMillis() {
        final Album album = this.musicDao.getCurrentAlbum();
        album.currentMillisInTrack = (int)this.exoPlayer.getCurrentPosition();
        this.musicDao.updateAlbum(album);
    }

    void release() {
        if (getPlayStatus() == PLAYING) {
            this.musicDao.setCurrentPlayStatus(PAUSED);
        }
        persistTrackMillis();
        this.exoPlayer.release();
    }

    interface OnUpdateListener {
        void onUpdate();
    }
}
