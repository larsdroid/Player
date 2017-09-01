package org.willemsens.player.view.main.music;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.view.main.music.songs.SongRecyclerViewAdapter;
import org.willemsens.player.view.main.music.songs.SongsFragment;

public class MusicFragment extends Fragment
        implements BottomNavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    private MenuItem previousMenuItem;

    public MusicFragment() {
    }

    public static MusicFragment newInstance() {
        return new MusicFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, view);

        addEventHandlers();

        viewPager.setAdapter(new MusicViewPagerAdapter(getChildFragmentManager()));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_music_menu, menu);
        getActivity().setTitle(R.string.title_music);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            switch (getCurrentFragment()) {
                case ALBUMS:
                    Toast.makeText(getActivity(), "FILTER " + (viewPager.getCurrentItem() + 1), Toast.LENGTH_SHORT).show();
                    break;
                case ARTISTS:
                    Toast.makeText(getActivity(), "FILTER " + (viewPager.getCurrentItem() + 1), Toast.LENGTH_SHORT).show();
                    break;
                case SONGS:
                    View filterMenuItemView = getActivity().findViewById(R.id.action_filter);
                    PopupMenu popup = new PopupMenu(getContext(), filterMenuItemView);
                    popup.setOnMenuItemClickListener(getSongsFragment());
                    popup.inflate(R.menu.fragment_songs_filter_menu);
                    SongRecyclerViewAdapter.SongFilter filter = getSongsFragment().getFilter();
                    if (filter.getAlbums().isEmpty()) {
                        popup.getMenu().findItem(R.id.menu_item_filter_songs_show_all).setEnabled(false);
                    }
                    popup.show();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final SubFragmentType subFragmentType = SubFragmentType.get(item.getItemId());
        if (subFragmentType != null) {
            return setCurrentFragment(subFragmentType);
        } else {
            return false;
        }
    }

    public boolean setCurrentFragment(SubFragmentType subFragmentType) {
        if (viewPager.getCurrentItem() != subFragmentType.getIndex()) {
            viewPager.setCurrentItem(subFragmentType.getIndex());
            return true;
        } else {
            return false;
        }
    }

    private SubFragmentType getCurrentFragment() {
        return SubFragmentType.getByIndex(viewPager.getCurrentItem());
    }

    private SongsFragment getSongsFragment() {
        return (SongsFragment) ((MusicViewPagerAdapter) viewPager.getAdapter()).getFragment(SubFragmentType.SONGS);
    }

    public void filterSongs(Album album) {
        SongRecyclerViewAdapter.SongFilter filter = getSongsFragment().getFilter();
        filter.clear();
        filter.add(album);
        filter.filter(null);
    }

    private void addEventHandlers() {
        navigation.setOnNavigationItemSelectedListener(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (previousMenuItem != null) {
                    previousMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                previousMenuItem = navigation.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}
