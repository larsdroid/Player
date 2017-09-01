package org.willemsens.player.view.main.music;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import org.willemsens.player.exceptions.PlayerException;
import org.willemsens.player.view.main.music.albums.AlbumsFragment;
import org.willemsens.player.view.main.music.artists.ArtistsFragment;
import org.willemsens.player.view.main.music.songs.SongsFragment;

class MusicViewPagerAdapter extends FragmentPagerAdapter {
    private final SparseArray<Fragment> fragments = new SparseArray<>();

    MusicViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        SubFragmentType subFragmentType = SubFragmentType.getByIndex(position);
        if (subFragmentType != null) {
            switch (subFragmentType) {
                case ALBUMS:
                    return AlbumsFragment.newInstance();
                case ARTISTS:
                    return ArtistsFragment.newInstance();
                case SONGS:
                default:
                    return SongsFragment.newInstance();
            }
        } else {
            throw new PlayerException("Index " + position + " is invalid for ViewPager.");
        }
    }

    Fragment getFragment(SubFragmentType subFragmentType) {
        return fragments.get(subFragmentType.getIndex());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return SubFragmentType.values().length;
    }
}
