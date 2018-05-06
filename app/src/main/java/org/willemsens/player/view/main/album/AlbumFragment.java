package org.willemsens.player.view.main.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.view.customviews.HeightCalculatedImageView;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;

public class AlbumFragment extends Fragment {
    private Album album;
    private AlbumSongAdapter adapter;
    private MusicDao musicDao;

    @BindView(R.id.album_image)
    HeightCalculatedImageView albumImage;

    @BindView(R.id.song_list)
    RecyclerView songList;

    public static AlbumFragment newInstance(final Context context, final long albumId) {
        final AlbumFragment theInstance = new AlbumFragment();

        theInstance.musicDao = AppDatabase.getAppDatabase(context).musicDao();

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
            final int albumId = arguments.getInt(MLBPT_ALBUM_ID.name());
            this.album = musicDao.findAlbum(albumId);

            final Image albumCover = musicDao.findImage(album.imageId);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(
                    albumCover.imageData, 0, albumCover.imageData.length);
            this.albumImage.setImageBitmap(bitmap);

            Context context = view.getContext();
            this.songList.setLayoutManager(new LinearLayoutManager(context));
            if (this.adapter == null) {
                this.adapter = new AlbumSongAdapter(context, this.album);
            }
            this.songList.setAdapter(this.adapter);
        }
        return view;
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
    }

    @Override
    public void onPause() {
        adapter.unregisterPlayBackUpdateReceiver();
        super.onPause();
    }
}
