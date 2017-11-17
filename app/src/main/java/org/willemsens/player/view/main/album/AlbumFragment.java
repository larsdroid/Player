package org.willemsens.player.view.main.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.view.DataAccessProvider;
import org.willemsens.player.view.customviews.HeightCalculatedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;
    private Album album;

    @BindView(R.id.album_image)
    HeightCalculatedImageView albumImage;

    public static AlbumFragment newInstance(final Context context, final long albumId) {
        final AlbumFragment theInstance = new AlbumFragment();

        Bundle args = new Bundle();
        args.putLong(context.getString(R.string.key_album_id), albumId);
        theInstance.setArguments(args);

        return theInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        if (arguments != null) {
            final long albumId = arguments.getLong(getString(R.string.key_album_id));
            this.album = this.dataAccessProvider.getMusicDao().findAlbum(albumId);

            final Bitmap bitmap = BitmapFactory.decodeByteArray(
                    album.getImage().getImageData(), 0, album.getImage().getImageData().length);
            this.albumImage.setImageBitmap(bitmap);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_album_menu, menu);
        getActivity().setTitle(this.album.getArtist().getName() + " - " + this.album.getName());
        super.onCreateOptionsMenu(menu, inflater);
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
}
