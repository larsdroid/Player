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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import org.willemsens.player.R;
import org.willemsens.player.model.Artist;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTISTS_DELETED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTISTS_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTIST_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTIST_UPDATED;

/**
 * A fragment representing a list of Artists.
 */
public class ArtistsFragment extends Fragment {
    private MusicDao musicDao;
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
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_artists_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            if (this.artists.isEmpty()) {
                loadAllArtists();
            }
            this.adapter = new ArtistRecyclerViewAdapter(context, this.artists, (OnArtistClickedListener) context);
            recyclerView.setAdapter(this.adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        musicDao = AppDatabase.getAppDatabase(context).musicDao();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(MLBT_ARTISTS_INSERTED.name());
        filter.addAction(MLBT_ARTIST_INSERTED.name());
        filter.addAction(MLBT_ARTIST_UPDATED.name());
        filter.addAction(MLBT_ARTISTS_DELETED.name());
        lbm.registerReceiver(this.dbUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.getActivity());
        lbm.unregisterReceiver(this.dbUpdateReceiver);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_artists_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private class DBUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(MLBT_ARTISTS_INSERTED.name())) {
                loadAllArtists();
                adapter.notifyDataSetChanged();
            } else if (intentAction.equals(MLBT_ARTIST_INSERTED.name())) {
                final int artistId = intent.getIntExtra(MLBPT_ARTIST_ID.name(), -1);
                final Artist artist = musicDao.findArtist(artistId);
                artists.add(artist);
                Collections.sort(artists);
                final int index = artists.indexOf(artist);
                adapter.notifyItemInserted(index);
            } else if (intentAction.equals(MLBT_ARTIST_UPDATED.name())) {
                final int artistId = intent.getIntExtra(MLBPT_ARTIST_ID.name(), -1);
                final Artist artist = musicDao.findArtist(artistId);
                final int index = artists.indexOf(artist);
                if (index != -1) {
                    artists.set(index, artist);
                    adapter.notifyItemChanged(index);
                }
            } else if (intentAction.equals(MLBT_ARTISTS_DELETED.name())) {
                artists.clear();
            }
        }
    }

    private void loadAllArtists() {
        artists.clear();
        artists.addAll(musicDao.getAllArtists());
    }
}
