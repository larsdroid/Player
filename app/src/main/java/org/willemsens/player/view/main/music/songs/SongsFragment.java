package org.willemsens.player.view.main.music.songs;

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
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Songs.
 */
public class SongsFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;
    private SongRecyclerViewAdapter adapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            if (this.songs.isEmpty()) {
                loadAllSongs();
            }
            this.adapter = new SongRecyclerViewAdapter(this.songs, (OnSongClickedListener) context);
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
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.key_songs_inserted));
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
            if (intentAction.equals(getString(R.string.key_songs_inserted))) {
                loadAllSongs();
                adapter.notifyDataSetChanged();
            } else if (intentAction.equals(getString(R.string.key_song_inserted))) {
                final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
                final Song song = dataAccessProvider.getMusicDao().findSong(songId);
                songs.add(song);
                Collections.sort(songs);
                final int index = songs.indexOf(song);
                adapter.notifyItemInserted(index);
            }
        }
    }

    private void loadAllSongs() {
        songs.clear();
        songs.addAll(dataAccessProvider.getMusicDao().getAllSongs());
        Collections.sort(songs);
    }

    public SongRecyclerViewAdapter getAdapter() {
        return adapter;
    }
}
