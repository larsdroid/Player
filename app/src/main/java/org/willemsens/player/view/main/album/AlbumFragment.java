package org.willemsens.player.view.main.album;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.view.customviews.HeightCalculatedImageView;
import org.willemsens.player.view.customviews.HeightCalculatedProgressBar;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_UPDATED;

public class AlbumFragment extends Fragment {
    private Album album;
    private AlbumSongAdapter adapter;
    private MusicDao musicDao;
    private AlbumUpdateReceiver updateReceiver;

    @BindView(R.id.album_image)
    HeightCalculatedImageView albumImage;

    @BindView(R.id.album_image_progress_bar)
    HeightCalculatedProgressBar progressBar;

    @BindView(R.id.song_list)
    RecyclerView songList;

    public static AlbumFragment newInstance(final Context context, final long albumId) {
        final AlbumFragment theInstance = new AlbumFragment();

        theInstance.musicDao = AppDatabase.getAppDatabase(context).musicDao();
        theInstance.updateReceiver = theInstance.new AlbumUpdateReceiver();

        Bundle args = new Bundle();
        args.putLong(MLBPT_ALBUM_ID.name(), albumId);
        theInstance.setArguments(args);

        return theInstance;
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
            this.album = musicDao.findAlbum(albumId);

            fetchAndShowAlbumArt();


            Context context = view.getContext();
            this.songList.setLayoutManager(new LinearLayoutManager(context));
            if (this.adapter == null) {
                this.adapter = new AlbumSongAdapter(context, this.album);
            }
            this.songList.setAdapter(this.adapter);
        }
        return view;
    }

    private void fetchAndShowAlbumArt() {
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
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_album_menu, menu);
        getActivity().setTitle(musicDao.findArtist(album.artistId).name + " - " + this.album.name);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.registerPlayBackUpdateReceiver();
        registerAlbumUpdateReceiver();
    }

    @Override
    public void onPause() {
        unregisterAlbumUpdateReceiver();
        adapter.unregisterPlayBackUpdateReceiver();
        super.onPause();
    }

    private void registerAlbumUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(MLBT_ALBUM_UPDATED.name());
        lbm.registerReceiver(this.updateReceiver, filter);
    }

    private void unregisterAlbumUpdateReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(this.updateReceiver);
    }

    private class AlbumUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction != null && intentAction.equals(MLBT_ALBUM_UPDATED.name())) {
                long albumId = intent.getLongExtra(MLBPT_ALBUM_ID.name(), -1);
                if (albumId == album.id && progressBar.getVisibility() == View.VISIBLE) {
                    album = musicDao.findAlbum(album.id);
                    fetchAndShowAlbumArt();
                }
            } else {
                Log.e(getClass().getName(), "Invalid Intent received. Action: '" + intentAction + "'");
            }
        }
    }
}
