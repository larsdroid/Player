package org.willemsens.player.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

class MainViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();

    MainViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }
}
