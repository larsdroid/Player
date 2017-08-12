package org.willemsens.player.view.albums;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Albums.
 */
public class AlbumsFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;
    private AlbumRecyclerViewAdapter adapter;
    private final DBUpdateReceiver dbUpdateReceiver;
    private final List<Album> albums;

    public AlbumsFragment() {
        this.dbUpdateReceiver = new DBUpdateReceiver();
        this.albums = new ArrayList<>();
    }

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            if (this.albums.isEmpty()) {
                loadAllAlbums();
            }
            this.adapter = new AlbumRecyclerViewAdapter(this.albums);
            recyclerView.setAdapter(this.adapter);
        }
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
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.key_albums_inserted));
        filter.addAction(getString(R.string.key_album_inserted));
        filter.addAction(getString(R.string.key_album_updated));
        lbm.registerReceiver(this.dbUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.getActivity());
        lbm.unregisterReceiver(this.dbUpdateReceiver);
        super.onPause();
    }

    private class DBUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(getString(R.string.key_albums_inserted))) {
                loadAllAlbums();
                adapter.notifyDataSetChanged();
            } else if (intentAction.equals(getString(R.string.key_album_inserted))) {
                final long albumId = intent.getLongExtra(getString(R.string.key_album_id), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                albums.add(album);
                Collections.sort(albums);
                final int index = albums.indexOf(album);
                adapter.notifyItemInserted(index);
            } else if (intentAction.equals(getString(R.string.key_album_updated))) {
                final long albumId = intent.getLongExtra(getString(R.string.key_album_id), -1);
                final Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
                final int index = albums.indexOf(album);
                if (index != -1) {
                    albums.set(index, album);
                    adapter.notifyItemChanged(index);
                }
            }
        }
    }

    private void loadAllAlbums() {
        albums.clear();
        albums.addAll(dataAccessProvider.getMusicDao().getAllAlbums());
        Collections.sort(albums);
    }
}
