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
                this.adapter = new SongRecyclerViewAdapter(this.dataAccessProvider, (OnSongClickedListener) context);
            }
            recyclerView.setAdapter(this.adapter);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAdapter().registerDbUpdateReceiver(this.getActivity());
    }

    @Override
    public void onPause() {
        getAdapter().unregisterDbUpdateReceiver(this.getActivity());
        super.onPause();
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