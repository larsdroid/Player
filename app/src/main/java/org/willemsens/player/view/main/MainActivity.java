package org.willemsens.player.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.services.AlbumInfoFetcherService;
import org.willemsens.player.services.ArtistInfoFetcherService;
import org.willemsens.player.services.FileScannerService;
import org.willemsens.player.services.MusicPlayingService;
import org.willemsens.player.view.DataAccessProvider;
import org.willemsens.player.view.albums.AlbumsFragment;
import org.willemsens.player.view.artists.ArtistsFragment;
import org.willemsens.player.view.settings.SettingsFragment;
import org.willemsens.player.view.songs.OnSongClickedListener;
import org.willemsens.player.view.songs.SongsFragment;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        DataAccessProvider,
        OnSongClickedListener,
        NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private MenuItem previousMenuItem;

    private EntityDataStore<Persistable> dataStore;
    private MusicDao musicDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle); // TODO
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        this.dataStore = ((PlayerApplication) getApplication()).getData();
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        /*

        TODO: code comes from NavigationView / DrawerLayout (left sliding menu thing). So both menu (bottom AND left slider) use this SAME method???

         if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

        */
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

    @Override
    public void songClicked(Song song) {
        Intent intent = new Intent(this, MusicPlayingService.class);
        intent.putExtra(getString(R.string.key_song_id), song.getId());
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // TODO
            Toast.makeText(this, "SETTINGS", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
