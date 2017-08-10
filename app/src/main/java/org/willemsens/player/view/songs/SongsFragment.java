package org.willemsens.player.view.songs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.view.DataAccessProvider;

import java.util.List;

/**
 * A fragment representing a list of Songs.
 */
public class SongsFragment extends Fragment {
    private DataAccessProvider dataAccessProvider;

    public SongsFragment() {
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
            final List<Song> songs = this.dataAccessProvider.getMusicDao().getAllSongs();
            final SongRecyclerViewAdapter adapter = new SongRecyclerViewAdapter(songs);
            recyclerView.setAdapter(adapter);
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
}
