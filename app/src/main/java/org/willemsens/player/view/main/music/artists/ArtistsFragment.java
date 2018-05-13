package org.willemsens.player.view.main.music.artists;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import org.willemsens.player.R;

/**
 * A fragment representing a list of Artists.
 */
public class ArtistsFragment extends Fragment {
    private ArtistRecyclerViewAdapter adapter;
    private ArtistsViewModel viewModel;

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_artists_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        this.viewModel = ViewModelProviders.of(this).get(ArtistsViewModel.class);

        this.adapter = new ArtistRecyclerViewAdapter((OnArtistClickedListener) context);
        recyclerView.setAdapter(this.adapter);

        observeArtists();

        return view;
    }

    private void observeArtists() {
        this.viewModel.artistsLiveData.observe(this,
                artists -> this.adapter.setArtists(artists));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_artists_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
