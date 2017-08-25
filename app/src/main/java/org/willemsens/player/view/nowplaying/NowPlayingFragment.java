package org.willemsens.player.view.nowplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.playback.PlayStatus;
import org.willemsens.player.view.DataAccessProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NowPlayingFragment extends Fragment {
    @BindView(R.id.now_playing_bar_image)
    ImageView albumCover;

    @BindView(R.id.now_playing_bar_track)
    TextView trackNumber;

    @BindView(R.id.now_playing_bar_song)
    TextView songName;

    @BindView(R.id.now_playing_bar_album)
    TextView albumName;

    @BindView(R.id.button_previous)
    ImageView previousButton;

    @BindView(R.id.button_play_pause_stop)
    ImageView playPauseStopButton;

    @BindView(R.id.button_next)
    ImageView nextButton;

    private DataAccessProvider dataAccessProvider;
    private final PlayBackStatusReceiver playBackStatusReceiver;

    public NowPlayingFragment() {
        this.playBackStatusReceiver = new PlayBackStatusReceiver();
    }

    public static NowPlayingFragment newInstance() {
        return new NowPlayingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataAccessProvider) {
            this.dataAccessProvider = (DataAccessProvider) context;
        } else {
            Log.e(getClass().getName(), "Context should be a DataAccessProvider.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.key_player_status_changed));
        lbm.registerReceiver(this.playBackStatusReceiver, filter);
    }

    private class PlayBackStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(getString(R.string.key_player_status_changed))) {
                final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
                final Song song = dataAccessProvider.getMusicDao().findSong(songId);
                final PlayStatus playStatus = PlayStatus.valueOf(intent.getStringExtra(getString(R.string.key_play_status)));
                switch (playStatus) {
                    case PLAYING:
                        playPauseStopButton.setImageResource(R.drawable.ic_pause_black_48dp);
                        break;
                    case STOPPED:
                        playPauseStopButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                        break;
                    case PAUSED:
                        playPauseStopButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                }

                if (song.getAlbum().getImage() != null) {
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(
                            song.getAlbum().getImage().getImageData(), 0, song.getAlbum().getImage().getImageData().length);
                    albumCover.setImageBitmap(bitmap);
                } else {
                    albumCover.setImageDrawable(null);
                }

                trackNumber.setText(String.valueOf(song.getTrack()));
                songName.setText(song.getName());
                albumName.setText(song.getAlbum().getName());
            }
        }
    }
}
