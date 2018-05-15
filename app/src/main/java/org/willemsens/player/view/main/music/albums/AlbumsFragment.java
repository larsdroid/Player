package org.willemsens.player.view.main.music.albums;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import org.willemsens.player.R;
import org.willemsens.player.persistence.entities.Artist;

import static android.view.Menu.NONE;

/**
 * A fragment representing a list of Albums.
 */
public class AlbumsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private AlbumRecyclerViewAdapter adapter;
    private AlbumsViewModel viewModel;

    public AlbumsFragment() {
    }

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_albums_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        this.viewModel = ViewModelProviders.of(this).get(AlbumsViewModel.class);

        this.adapter = new AlbumRecyclerViewAdapter(context, savedInstanceState);
        recyclerView.setAdapter(this.adapter);

        observeAlbums();
        observeArtists();

        return view;
    }

    private void observeAlbums() {
        this.viewModel.albumsLiveData.observe(this,
                albums -> this.adapter.setAllAlbums(albums));
    }

    private void observeArtists() {
        this.viewModel.artistsLiveData.observe(this,
                artists -> this.adapter.setArtists(artists));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // It's possible that Android tries to save this instance even though it has never been shown
        // (no call happened to onCreateView). As a result, the adapter can be null here.
        if (adapter != null) {
            adapter.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_albums_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            View filterMenuItemView = getActivity().findViewById(R.id.action_filter);
            PopupMenu popup = new PopupMenu(getContext(), filterMenuItemView);
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.fragment_albums_filter_menu);
            AlbumRecyclerViewAdapter.AlbumFilter filter = getFilter();
            if (filter.hasAllArtists()) {
                popup.getMenu().findItem(R.id.menu_item_filter_albums_show_all).setEnabled(false);
            }
            popup.show();
            return true;
        } else {
            return false;
        }
    }

    public AlbumRecyclerViewAdapter.AlbumFilter getFilter() {
        return (AlbumRecyclerViewAdapter.AlbumFilter) adapter.getFilter();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        final AlbumRecyclerViewAdapter.AlbumFilter filter = getFilter();
        if (id == R.id.menu_item_filter_albums_show_all) {
            filter.addAllArtists();
            filter.filter(null);
        } else if (id == R.id.menu_item_filter_albums_by_artist) {
            SubMenu artistsMenu = item.getSubMenu();
            int i = 0;
            final MenuItem menuItemAll = artistsMenu.getItem(i++);
            menuItemAll.setCheckable(true);
            menuItemAll.setChecked(true);

            if (this.viewModel.artistsLiveData.getValue() != null) {
                for (Artist artist : this.viewModel.artistsLiveData.getValue()) {
                    artistsMenu.add(NONE, (int) artist.id, NONE, artist.name);
                    final MenuItem menuItem = artistsMenu.getItem(i++);
                    menuItem.setCheckable(true);
                    if (filter.getArtistValue(artist.id)) {
                        menuItem.setChecked(true);
                    } else {
                        menuItemAll.setChecked(false);
                    }
                }
            }
        } else if (id == R.id.menu_item_filter_all_artists) {
            if (item.isChecked()) {
                filter.removeAllArtists();
            } else {
                filter.addAllArtists();
            }
            filter.filter(null);
        } else {
            filter.flipArtist(item.getItemId());
            filter.filter(null);
        }
        return true;
    }
}
