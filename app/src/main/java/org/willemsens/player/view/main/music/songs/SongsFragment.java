package org.willemsens.player.view.main.music.songs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.view.DataAccessProvider;

import java.util.Iterator;
import java.util.Map;

import static android.view.Menu.NONE;

/**
 * A fragment representing a list of Songs.
 */
public class SongsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private DataAccessProvider dataAccessProvider;
    private SongRecyclerViewAdapter adapter;

    public SongsFragment() {
    }

    public static SongsFragment newInstance() {
        return new SongsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_songs_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            if (this.adapter == null) {
                this.adapter = new SongRecyclerViewAdapter(context, this.dataAccessProvider, savedInstanceState);
            }
            recyclerView.setAdapter(this.adapter);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.registerDbUpdateReceiver();
    }

    @Override
    public void onPause() {
        adapter.unregisterDbUpdateReceiver();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // It's possible that Android tries to save this instance even though it has never been shown
        // (no call happened to onCreateView). As a result, the adapter can be null here.
        if (adapter != null) {
            adapter.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_songs_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            View filterMenuItemView = getActivity().findViewById(R.id.action_filter);
            PopupMenu popup = new PopupMenu(getContext(), filterMenuItemView);
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.fragment_songs_filter_menu);
            SongRecyclerViewAdapter.SongFilter filter = getFilter();
            if (filter.hasAllAlbums() && filter.hasAllArtists()) {
                popup.getMenu().findItem(R.id.menu_item_filter_songs_show_all).setEnabled(false);
            }
            popup.show();
            return true;
        } else {
            return false;
        }
    }

    public SongRecyclerViewAdapter.SongFilter getFilter() {
         return (SongRecyclerViewAdapter.SongFilter) adapter.getFilter();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        final SongRecyclerViewAdapter.SongFilter filter = getFilter();
        if (id == R.id.menu_item_filter_songs_show_all) {
            filter.addAllAlbums();
            filter.addAllArtists();
            filter.filter(null);
        } else if (id == R.id.menu_item_filter_songs_by_album) {
            SubMenu albumsMenu = item.getSubMenu();
            int i = 0;
            final MenuItem menuItemAll = albumsMenu.getItem(i++);
            menuItemAll.setCheckable(true);
            menuItemAll.setChecked(true);
            for (Iterator<Map.Entry<Album, Boolean>> it = filter.getAlbumIterator(); it.hasNext();) {
                final Map.Entry<Album, Boolean> entry = it.next();
                final Album album = entry.getKey();
                albumsMenu.add(NONE, (int)album.getId().longValue(), NONE, album.getName());
                final MenuItem menuItem = albumsMenu.getItem(i++);
                menuItem.setCheckable(true);
                if (entry.getValue()) {
                    menuItem.setChecked(true);
                } else {
                    menuItemAll.setChecked(false);
                }
            }
        } else if (id == R.id.menu_item_filter_songs_by_artist) {
            SubMenu artistsMenu = item.getSubMenu();
            int i = 0;
            final MenuItem menuItemAll = artistsMenu.getItem(i++);
            menuItemAll.setCheckable(true);
            menuItemAll.setChecked(true);
            for (Iterator<Map.Entry<Artist, Boolean>> it = filter.getArtistIterator(); it.hasNext();) {
                final Map.Entry<Artist, Boolean> entry = it.next();
                final Artist artist = entry.getKey();
                // Negative ID means it's an artist
                artistsMenu.add(NONE, (int)-artist.getId(), NONE, artist.getName());
                final MenuItem menuItem = artistsMenu.getItem(i++);
                menuItem.setCheckable(true);
                if (entry.getValue()) {
                    menuItem.setChecked(true);
                } else {
                    menuItemAll.setChecked(false);
                }
            }
        } else if (id == R.id.menu_item_filter_all_albums) {
            if (item.isChecked()) {
                filter.removeAllAlbums();
            } else {
                filter.addAllAlbums();
            }
            filter.filter(null);
        } else if (id == R.id.menu_item_filter_all_artists) {
            if (item.isChecked()) {
                filter.removeAllArtists();
            } else {
                filter.addAllArtists();
            }
            filter.filter(null);
        } else {
            int itemId = item.getItemId();
            if (itemId > 0) {
                filter.flipAlbum(itemId);
            } else {
                // Negative ID means it's an artist
                filter.flipArtist(-itemId);
            }
            filter.filter(null);
        }
        return true;
    }
}
