package org.willemsens.player.view.main.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.willemsens.player.R;
import org.willemsens.player.view.DataAccessProvider;

public class SettingsFragment extends Fragment {
    private OnSettingsFragmentListener listener;
    private DataAccessProvider dataAccessProvider;
    private MusicDirectoryAdapter adapter;

    @BindView(R.id.directory_list)
    RecyclerView directoryList;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ButterKnife.bind(this, view);

        Context context = view.getContext();
        this.directoryList.setLayoutManager(new LinearLayoutManager(context));
        if (this.adapter == null) {
            this.adapter = new MusicDirectoryAdapter(this.dataAccessProvider);
        }
        this.directoryList.setAdapter(this.adapter);

        return view;
    }

    @OnClick(R.id.button_clear)
    public void clear() {
        this.listener.onClearMusicCache();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_settings_menu, menu);
        getActivity().setTitle(R.string.title_settings);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsFragmentListener
                && context instanceof DataAccessProvider) {
            listener = (OnSettingsFragmentListener) context;
            dataAccessProvider = (DataAccessProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsFragmentListener and DataAccessProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
