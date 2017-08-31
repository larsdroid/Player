package org.willemsens.player.view.main.music.songs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Song;
import org.willemsens.player.view.DataAccessProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.Menu.NONE;

/**
 * A fragment representing a list of Songs.
 */
public class SongsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
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

    public SongRecyclerViewAdapter.SongFilter getFilter() {
         return (SongRecyclerViewAdapter.SongFilter) getAdapter().getFilter();
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_filter_songs_show_all) {
            SongRecyclerViewAdapter.SongFilter filter = getFilter();
            filter.clear();
            filter.filter(null);
        } else if (id == R.id.menu_item_filter_songs_by_album) {
            SubMenu albumsMenu = item.getSubMenu();
            SongRecyclerViewAdapter.SongFilter filter = getFilter();
            int i = 0;
            for (Album album : dataAccessProvider.getMusicDao().getAllAlbums()) {
                albumsMenu.add(NONE, (int)album.getId().longValue(), NONE, album.getName());
                final MenuItem menuItem = albumsMenu.getItem(i++);
                menuItem.setCheckable(true);
                if (filter.getAlbums().contains(album)) {
                    menuItem.setChecked(true);
                }
            }
        } else {
            int albumId = item.getItemId();
            Album album = dataAccessProvider.getMusicDao().findAlbum(albumId);
            SongRecyclerViewAdapter.SongFilter filter = getFilter();
            if (filter.getAlbums().contains(album)) {
                filter.remove(album);
            } else {
                filter.add(album);
            }
            filter.filter(null);
        }
        return true;
    }
}
