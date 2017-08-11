package org.willemsens.player.view.songs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Songs.
 */
public class SongsFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;
    private final DBUpdateReceiver dbUpdateReceiver;
    private final List<Song> songs;

    public SongsFragment() {
        this.dbUpdateReceiver = new DBUpdateReceiver();
        this.songs = new ArrayList<>();
    }

    public static SongsFragment newInstance() {
        return new SongsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            if (this.songs.isEmpty()) {
                this.songs.addAll(this.dataAccessProvider.getMusicDao().getAllSongs());
            }
            recyclerView.setAdapter(new SongRecyclerViewAdapter(this.songs));
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
        lbm.registerReceiver(this.dbUpdateReceiver, new IntentFilter(getString(R.string.key_songs_inserted)));
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
            RecyclerView view = (RecyclerView)SongsFragment.this.getView();
            SongRecyclerViewAdapter adapter = (SongRecyclerViewAdapter)view.getAdapter();
            songs.clear();
            songs.addAll(SongsFragment.this.dataAccessProvider.getMusicDao().getAllSongs());
            adapter.notifyDataSetChanged();
        }
    }
}
