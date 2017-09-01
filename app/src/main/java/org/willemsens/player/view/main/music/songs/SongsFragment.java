package org.willemsens.player.view.main.music.songs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import org.willemsens.player.model.Artist;
import org.willemsens.player.view.DataAccessProvider;

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
        getAdapter().registerDbUpdateReceiver();
    }

    @Override
    public void onPause() {
        getAdapter().unregisterDbUpdateReceiver();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        getAdapter().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public SongRecyclerViewAdapter.SongFilter getFilter() {
         return (SongRecyclerViewAdapter.SongFilter) getAdapter().getFilter();
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
            // MenuItem "All":
            MenuItem menuItem = albumsMenu.getItem(i++);
            menuItem.setCheckable(true);
            if (filter.getAlbums().isEmpty()) {
                menuItem.setChecked(true);
            }
            for (Album album : dataAccessProvider.getMusicDao().getAllAlbums()) {
                albumsMenu.add(NONE, (int)album.getId().longValue(), NONE, album.getName());
                menuItem = albumsMenu.getItem(i++);
                menuItem.setCheckable(true);
                if (filter.getAlbums().isEmpty() || filter.getAlbums().contains(album)) {
                    menuItem.setChecked(true);
                }
            }
        } else if (id == R.id.menu_item_filter_songs_by_artist) {
            SubMenu artistsMenu = item.getSubMenu();
            SongRecyclerViewAdapter.SongFilter filter = getFilter();
            int i = 0;
            // MenuItem "All":
            MenuItem menuItem = artistsMenu.getItem(i++);
            menuItem.setCheckable(true);
            if (filter.getArtists().isEmpty()) {
                menuItem.setChecked(true);
            }
            for (Artist artist : dataAccessProvider.getMusicDao().getAllArtists()) {
                // Negative ID means it's an artist
                artistsMenu.add(NONE, (int)-artist.getId(), NONE, artist.getName());
                menuItem = artistsMenu.getItem(i++);
                menuItem.setCheckable(true);
                if (filter.getArtists().isEmpty() || filter.getArtists().contains(artist)) {
                    menuItem.setChecked(true);
                }
            }
        } else if (id == R.id.menu_item_filter_all_albums) {
            // TODO: tri-state!
        } else if (id == R.id.menu_item_filter_all_artists) {
            // TODO: tri-state!
        } else {
            int itemId = item.getItemId();
            if (itemId > 0) {
                Album album = dataAccessProvider.getMusicDao().findAlbum(itemId);
                SongRecyclerViewAdapter.SongFilter filter = getFilter();
                if (filter.getAlbums().contains(album)) {
                    filter.remove(album);
                } else if (filter.getAlbums().isEmpty()) {
                    filter.addAllAlbums(dataAccessProvider.getMusicDao().getAllAlbums());
                    filter.remove(album);
                } else {
                    filter.add(album);
                }
                filter.filter(null);
            } else {
                itemId = -itemId;
                // TODO: arists
            }
        }
        return true;
    }
}
