package org.willemsens.player.view.main.album;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.otto.Subscribe;
import org.willemsens.player.R;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Song;
import org.willemsens.player.playback.eventbus.AlbumProgressUpdatedMessage;
import org.willemsens.player.playback.eventbus.CurrentAlbumOrSongMessage;
import org.willemsens.player.playback.eventbus.CurrentPlayStatusMessage;
import org.willemsens.player.playback.eventbus.PlayBackEventBus;
import org.willemsens.player.util.StringFormat;
import org.willemsens.player.view.customviews.ClickableImageButton;
import org.willemsens.player.view.customviews.HeightCalculatedImageView;
import org.willemsens.player.view.customviews.HeightCalculatedProgressBar;
import org.willemsens.player.view.main.MainActivity;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.playback.PlayStatus.PLAYING;

public class AlbumFragment extends Fragment {
    private static final boolean SHOW_TITLE_IN_TOOLBAR = false;

    private AlbumSongAdapter adapter;
    private AlbumAndSongsViewModel viewModel;
    private OnPlayAlbumListener listener;

    private long albumId; // The ID of the album that this fragment is displaying.

    @BindView(R.id.album_image)
    HeightCalculatedImageView albumImage;

    @BindView(R.id.album_image_progress_bar)
    HeightCalculatedProgressBar progressBar;

    @BindView(R.id.song_list)
    RecyclerView songList;

    @BindView(R.id.album_toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.button_play_album)
    ClickableImageButton playAlbum;

    @BindView(R.id.album_name)
    TextView albumName;

    @BindView(R.id.artist_name)
    TextView artistName;

    @BindView(R.id.album_year)
    TextView albumYear;

    @BindView(R.id.album_length)
    TextView albumLength;

    @BindView(R.id.album_plays)
    TextView albumPlays;

    @BindView(R.id.times_played)
    TextView timesPlayed;

    @BindView(R.id.album_progress)
    ProgressBar albumProgress;

    public static AlbumFragment newInstance(final long albumId) {
        final AlbumFragment theInstance = new AlbumFragment();

        Bundle args = new Bundle();
        args.putLong(MLBPT_ALBUM_ID.name(), albumId);
        theInstance.setArguments(args);

        return theInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        if (arguments != null) {
            this.albumId = arguments.getLong(MLBPT_ALBUM_ID.name());

            this.viewModel = ViewModelProviders.of(this, new AlbumAndSongsViewModelFactory(getActivity().getApplication(), albumId)).get(AlbumAndSongsViewModel.class);
        }

        Context context = view.getContext();
        this.listener = (OnPlayAlbumListener) context;
        this.songList.setLayoutManager(new LinearLayoutManager(context));
        if (this.adapter == null) {
            this.adapter = new AlbumSongAdapter(context);
        }
        this.songList.setAdapter(this.adapter);

        this.albumImage.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.GONE);

        observeAlbum();
        observeArtist();
        observeSongs();
        observeCoverArt();

        this.playAlbum.setOnClickListener(event -> listener.playAlbum(this.albumId));

        return view;
    }

    private void observeAlbum() {
        this.viewModel.albumLiveData.observe(this, this::showAlbumInfo);
    }

    private void observeCoverArt() {
        this.viewModel.coverArtLiveData.observe(this, coverArt -> {
            if (coverArt != null) {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        coverArt.imageData, 0, coverArt.imageData.length);
                this.albumImage.setImageBitmap(bitmap);

                this.albumImage.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            } else {
                this.albumImage.setVisibility(View.GONE);
                this.progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_album_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().hide();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().show();
    }

    @Override
    public void onStart() {
        super.onStart();
        PlayBackEventBus.register(this);
    }

    @Override
    public void onStop() {
        PlayBackEventBus.unregister(this);

        MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().hide();
        activity.setSupportActionBar(getActivity().findViewById(R.id.main_toolbar));
        activity.getSupportActionBar().show();
        activity.setupActionBarAndDrawer();

        super.onStop();
    }

    private void updateAlbumProgress(Integer currentTrack, Integer currentMillisInTrack, Integer length) {
        if (this.viewModel.songsLiveData.getValue() != null
                && currentTrack != null
                && length != null) {
            double currentMillisInAlbum = 0.0;
            boolean foundCurrentSong = false;

            for (Song song : this.viewModel.songsLiveData.getValue()) {
                if (song.track == currentTrack) {
                    foundCurrentSong = true;
                    currentMillisInAlbum += currentMillisInTrack == null ? 0 : currentMillisInTrack / 1000;
                    break;
                } else {
                    currentMillisInAlbum += song.length;
                }
            }

            if (!foundCurrentSong) {
                Log.e(getClass().getName(), "Current track in album was not found within album!");
                albumProgress.setProgress(0);
            } else {
                albumProgress.setProgress((int) (currentMillisInAlbum * 100.0 / length));
            }
        } else {
            albumProgress.setProgress(0);
        }
    }

    private void observeArtist() {
        this.viewModel.artistLiveData.observe(this,
                artist -> {
                    setTitle();
                    adapter.setArtist(artist);
                    artistName.setText(artist.name);
                });
    }

    private void setTitle() {
        if (SHOW_TITLE_IN_TOOLBAR
                && viewModel.artistLiveData.getValue() != null
                && viewModel.albumLiveData.getValue() != null) {
            collapsingToolbarLayout.setTitle(viewModel.artistLiveData.getValue().name
                    + " - " + viewModel.albumLiveData.getValue().name);
        } else {
            collapsingToolbarLayout.setTitle(getString(R.string.play_album));
        }
    }

    private void observeSongs() {
        this.viewModel.songsLiveData.observe(this,
                songs -> {
                    adapter.setSongs(songs);
                    if (viewModel.albumLiveData.getValue() != null) {
                        final Album album = viewModel.albumLiveData.getValue();
                        updateAlbumProgress(album.currentTrack, album.currentMillisInTrack, album.length);
                    }
                });
    }

    @Subscribe
    public void handleCurrentAlbumOrSong(CurrentAlbumOrSongMessage message) {
        if (this.albumId == message.getAlbumId()) {
            adapter.setHighlightedSong(message.getSongId());
        } else {
            adapter.setHighlightedSong(null);
        }
    }

    private void showAlbumInfo(Album album) {
        setTitle();

        if (album != null) {
            albumName.setText(album.name);
            if (album.yearReleased != null) {
                albumYear.setText(String.valueOf(album.yearReleased));
            }
            if (album.length != null) {
                albumLength.setText(StringFormat.formatToSongLength(album.length));
            }

            /*
            TODO: implement this feature
            albumPlays.setText(String.valueOf(album.playCount));
            if (album.playCount == 1) {
                timesPlayed.setText(R.string.time_played);
            } else {
                timesPlayed.setText(R.string.times_played);
            }
            */
        }
    }

    @Subscribe
    public void handleAlbumProgressUpdated(AlbumProgressUpdatedMessage message) {
        if (this.albumId == message.getAlbumId()) {
            final Album album = this.viewModel.albumLiveData.getValue();
            updateAlbumProgress(message.getCurrentTrack(), message.getCurrentMillisInTrack(), album == null ? null : album.length);
        }
    }

    @Subscribe
    public void handleCurrentPlayStatus(CurrentPlayStatusMessage message) {
        // TODO: Fix this, on the 'release' build it's not working due to the livedata not working across processes.
        if (!adapter.isSongHighlighted()
                || message.getPlayStatus() != PLAYING) {
            this.playAlbum.setEnabled(true);
        } else {
            this.playAlbum.setEnabled(false);
        }
    }

    public interface OnPlayAlbumListener {
        void playAlbum(long albumId);
    }
}
