package org.willemsens.player.view.main.album;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

    public static AlbumFragment newInstance(final Context context, final long albumId) {
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
            this.viewModel = ViewModelProviders.of(this, new AlbumAndSongsViewModelFactory(getContext(), albumId)).get(AlbumAndSongsViewModel.class);
        }

        fetchAndShowAlbumArt();

        Context context = view.getContext();
        this.songList.setLayoutManager(new LinearLayoutManager(context));
        if (this.adapter == null) {
            this.adapter = new AlbumSongAdapter(context);
        }
        this.songList.setAdapter(this.adapter);

        observeAlbum();
        observeSongs();

        return view;
    }

    private void fetchAndShowAlbumArt() {
        /* TODO
        if (album.imageId != null) {
            final Image albumCover = musicDao.findImage(album.imageId);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(
                    albumCover.imageData, 0, albumCover.imageData.length);
            this.albumImage.setImageBitmap(bitmap);

            this.albumImage.setVisibility(View.VISIBLE);
            this.progressBar.setVisibility(View.GONE);
        } else {
            this.albumImage.setVisibility(View.GONE);
            this.progressBar.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_album_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.registerPlayBackUpdateReceiver();
    }

    @Override
    public void onPause() {
        adapter.unregisterPlayBackUpdateReceiver();
        super.onPause();
    }

    private void observeAlbum() {
        this.viewModel.albumLiveData.observe(this, album -> {
            // TODO: next line used to be in onCreateOptionsMenu... WHY??
            getActivity().setTitle(/* TODO musicDao.findArtist(album.artistId).name + " - " +*/ viewModel.albumLiveData.getValue().name);
        });
    }

    private void observeSongs() {
        this.viewModel.songsLiveData.observe(this, songs -> {
            adapter.setSongs(songs);
        });
    }

    // TODO: Intent action MLBT_ALBUM_UPDATED no longer accepted here
    // TODO: Intent extra MLBPT_ALBUM_ID no longer accepted here
}
