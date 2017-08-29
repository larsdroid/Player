package org.willemsens.player.view.main.music;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.view.main.music.albums.AlbumsFragment;
import org.willemsens.player.view.main.music.artists.ArtistsFragment;
import org.willemsens.player.view.main.music.songs.SongRecyclerViewAdapter;
import org.willemsens.player.view.main.music.songs.SongsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        setupViewPager();

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
            // TODO
            Toast.makeText(getActivity(), "FILTER " + (viewPager.getCurrentItem() + 1), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public enum SubFragment {
        ALBUMS(0, R.id.navigation_albums), ARTISTS(1, R.id.navigation_artists), SONGS(2, R.id.navigation_songs);

        private int index;

        @IdRes
        private int menuItemResId;

        SubFragment(int index, @IdRes int menuItemResId) {
            this.index = index;
            this.menuItemResId = menuItemResId;
        }

        public int getIndex() {
            return index;
        }

        public static SubFragment get(@IdRes int fragmentResId) {
            for (SubFragment subFragment : values()) {
                if (subFragment.menuItemResId == fragmentResId) {
                    return subFragment;
                }
            }
            return null;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final SubFragment subFragment = SubFragment.get(item.getItemId());
        if (subFragment != null && viewPager.getCurrentItem() != subFragment.getIndex()) {
            viewPager.setCurrentItem(subFragment.getIndex());
            return true;
        } else {
            return false;
        }
    }

    public void setCurrentFragment(SubFragment subFragment) {
        if (viewPager.getCurrentItem() != subFragment.getIndex()) {
            viewPager.setCurrentItem(subFragment.getIndex());
        }
    }

    public void filterSongs(Album album) {
        SongsFragment songsFragment = (SongsFragment)((MusicViewPagerAdapter)viewPager.getAdapter()).getItem(2);
        SongRecyclerViewAdapter.SongFilter filter = (SongRecyclerViewAdapter.SongFilter)songsFragment.getAdapter().getFilter();
        filter.setAlbum(album);
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

    private void setupViewPager() {
        MusicViewPagerAdapter adapter = new MusicViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(AlbumsFragment.newInstance());
        adapter.addFragment(ArtistsFragment.newInstance());
        adapter.addFragment(SongsFragment.newInstance());
        viewPager.setAdapter(adapter);
    }
}
