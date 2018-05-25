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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.view.customviews.HeightCalculatedImageView;
import org.willemsens.player.view.customviews.HeightCalculatedProgressBar;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;

public class AlbumFragment extends Fragment {
    private AlbumSongAdapter adapter;
    private AlbumAndSongsViewModel viewModel;

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
            final long albumId = arguments.getLong(MLBPT_ALBUM_ID.name());
            this.viewModel = ViewModelProviders.of(this, new AlbumAndSongsViewModelFactory(getActivity().getApplication(), albumId)).get(AlbumAndSongsViewModel.class);
        }

        Context context = view.getContext();
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
        observeCurrentSong();

        return view;
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
    public void onStop() {
        super.onStop();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().hide();
        activity.setSupportActionBar(getActivity().findViewById(R.id.main_toolbar));
        activity.getSupportActionBar().show();
    }

    private void observeAlbum() {
        this.viewModel.albumLiveData.observe(this,
                album -> setTitle());
    }

    private void observeArtist() {
        this.viewModel.artistLiveData.observe(this,
                artist -> {
                    setTitle();
                    adapter.setArtist(artist);
                });
    }

    private void setTitle() {
        if (viewModel.artistLiveData.getValue() != null
                && viewModel.albumLiveData.getValue() != null) {
            collapsingToolbarLayout.setTitle(viewModel.artistLiveData.getValue().name
                    + " - " + viewModel.albumLiveData.getValue().name);
        }
    }

    private void observeSongs() {
        this.viewModel.songsLiveData.observe(this,
                songs -> adapter.setSongs(songs));
    }

    private void observeCurrentSong() {
        this.viewModel.currentSongLiveData.observe(this,
                currentSong -> adapter.setHighlightedSong(currentSong));
    }
}
