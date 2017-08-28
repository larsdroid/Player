package org.willemsens.player.view.main.music.artists;

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
import org.willemsens.player.model.Artist;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Artists.
 */
public class ArtistsFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;
    private ArtistRecyclerViewAdapter adapter;
    private final DBUpdateReceiver dbUpdateReceiver;
    private final List<Artist> artists;

    public ArtistsFragment() {
        this.dbUpdateReceiver = new DBUpdateReceiver();
        this.artists = new ArrayList<>();
    }

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            if (this.artists.isEmpty()) {
                loadAllArtists();
            }
            this.adapter = new ArtistRecyclerViewAdapter(this.artists);
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
        filter.addAction(getString(R.string.key_artists_inserted));
        filter.addAction(getString(R.string.key_artist_inserted));
        filter.addAction(getString(R.string.key_artist_updated));
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
            if (intentAction.equals(getString(R.string.key_artists_inserted))) {
                loadAllArtists();
                adapter.notifyDataSetChanged();
            } else if (intentAction.equals(getString(R.string.key_artist_inserted))) {
                final long artistId = intent.getLongExtra(getString(R.string.key_artist_id), -1);
                final Artist artist = dataAccessProvider.getMusicDao().findArtist(artistId);
                artists.add(artist);
                Collections.sort(artists);
                final int index = artists.indexOf(artist);
                adapter.notifyItemInserted(index);
            } else if (intentAction.equals(getString(R.string.key_artist_updated))) {
                final long artistId = intent.getLongExtra(getString(R.string.key_artist_id), -1);
                final Artist artist = dataAccessProvider.getMusicDao().findArtist(artistId);
                final int index = artists.indexOf(artist);
                if (index != -1) {
                    artists.set(index, artist);
                    adapter.notifyItemChanged(index);
                }
            }
        }
    }

    private void loadAllArtists() {
        artists.clear();
        artists.addAll(dataAccessProvider.getMusicDao().getAllArtists());
        Collections.sort(artists);
    }
}
