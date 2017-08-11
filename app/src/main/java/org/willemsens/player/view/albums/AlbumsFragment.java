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
import java.util.List;

/**
 * A fragment representing a list of Albums.
 */
public class AlbumsFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;
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
                this.albums.addAll(this.dataAccessProvider.getMusicDao().getAllAlbums());
            }
            recyclerView.setAdapter(new AlbumRecyclerViewAdapter(this.albums));
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
        lbm.registerReceiver(this.dbUpdateReceiver, new IntentFilter(getString(R.string.key_albums_inserted)));
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
            RecyclerView view = (RecyclerView)AlbumsFragment.this.getView();
            AlbumRecyclerViewAdapter adapter = (AlbumRecyclerViewAdapter)view.getAdapter();
            albums.clear();
            albums.addAll(AlbumsFragment.this.dataAccessProvider.getMusicDao().getAllAlbums());
            adapter.notifyDataSetChanged();
        }
    }
}
