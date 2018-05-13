package org.willemsens.player.view.main.music;

import android.os.Bundle;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import org.willemsens.player.R;
import org.willemsens.player.view.main.music.albums.AlbumRecyclerViewAdapter;
import org.willemsens.player.view.main.music.albums.AlbumsFragment;

import static org.willemsens.player.view.main.music.SubFragmentType.ALBUMS;

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
        getActivity().setTitle(R.string.title_music);
        super.onCreateOptionsMenu(menu, inflater);
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

    private AlbumsFragment getAlbumssFragment() {
        return (AlbumsFragment) ((MusicViewPagerAdapter) viewPager.getAdapter()).getFragment(ALBUMS);
    }

    public void filterAlbums(long artistId) {
        AlbumRecyclerViewAdapter.AlbumFilter filter = getAlbumssFragment().getFilter();
        filter.removeAllArtists();
        filter.add(artistId);
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
