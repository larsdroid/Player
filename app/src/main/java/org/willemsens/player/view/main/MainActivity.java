package org.willemsens.player.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.services.ArtistInfoFetcherService;
import org.willemsens.player.services.FileScannerService;
import org.willemsens.player.services.AlbumInfoFetcherService;
import org.willemsens.player.view.DataAccessProvider;
import org.willemsens.player.view.albums.AlbumsFragment;
import org.willemsens.player.view.artists.ArtistsFragment;
import org.willemsens.player.view.settings.SettingsFragment;
import org.willemsens.player.view.songs.SongsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        DataAccessProvider {
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    private MenuItem previousMenuItem;

    private EntityDataStore<Persistable> dataStore;
    private MusicDao musicDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.dataStore = ((PlayerApplication)getApplication()).getData();
        this.musicDao = new MusicDao(this.dataStore);

        Intent intent = new Intent(this, FileScannerService.class);
        startService(intent);

        intent = new Intent(this, AlbumInfoFetcherService.class);
        startService(intent);

        intent = new Intent(this, ArtistInfoFetcherService.class);
        startService(intent);

        addEventHandlers();
        setupViewPager();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int newItemIndex = -1;
        switch (item.getItemId()) {
            case R.id.navigation_albums:
                newItemIndex = 0;
                break;
            case R.id.navigation_artists:
                newItemIndex = 1;
                break;
            case R.id.navigation_songs:
                newItemIndex = 2;
                break;
            case R.id.navigation_settings:
                newItemIndex = 3;
        }
        if (viewPager.getCurrentItem() != newItemIndex) {
            viewPager.setCurrentItem(newItemIndex);
            return true;
        } else {
            return false;
        }
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
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(AlbumsFragment.newInstance());
        adapter.addFragment(ArtistsFragment.newInstance());
        adapter.addFragment(SongsFragment.newInstance());
        adapter.addFragment(SettingsFragment.newInstance("", ""));
        viewPager.setAdapter(adapter);
    }

    @Override
    public MusicDao getMusicDao() {
        return this.musicDao;
    }
}
