package org.willemsens.player.view.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.fetchers.AlbumInfoFetcherService;
import org.willemsens.player.fetchers.ArtistInfoFetcherService;
import org.willemsens.player.filescanning.FileScannerService;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.playback.PlayBackIntentBuilder;
import org.willemsens.player.playback.PlayStatus;
import org.willemsens.player.view.DataAccessProvider;
import org.willemsens.player.view.main.album.AlbumFragment;
import org.willemsens.player.view.main.music.MusicFragment;
import org.willemsens.player.view.main.music.SubFragmentType;
import org.willemsens.player.view.main.music.albums.OnAlbumClickedListener;
import org.willemsens.player.view.main.music.artists.OnArtistClickedListener;
import org.willemsens.player.view.main.music.nowplaying.NowPlayingFragment;
import org.willemsens.player.view.main.music.songs.OnSongClickedListener;
import org.willemsens.player.view.main.settings.OnSettingsFragmentListener;
import org.willemsens.player.view.main.settings.SettingsFragment;

import static org.willemsens.player.playback.PlayBackBroadcastType.PBBT_PLAYER_STATUS_UPDATE;
import static org.willemsens.player.playback.PlayStatus.STOPPED;
import static org.willemsens.player.playback.PlayerCommand.PAUSE;
import static org.willemsens.player.playback.PlayerCommand.PLAY;

public class MainActivity extends AppCompatActivity
        implements DataAccessProvider,
        OnSongClickedListener,
        OnArtistClickedListener,
        OnAlbumClickedListener,
        OnSettingsFragmentListener,
        NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CODE_INTERNET = 2;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private Integer currentMenuItem;
    private MusicDao musicDao;
    private PlayBackStatusReceiver playBackStatusReceiver;
    private HeadsetReceiver headsetReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final EntityDataStore<Persistable> dataStore = ((PlayerApplication) getApplication()).getData();
        this.musicDao = new MusicDao(dataStore, this);

        handlePermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE,
                getString(R.string.read_external_storage_required),
                new Runnable() {
                    @Override
                    public void run() {
                        setupAfterPermissionReadExternalStorage();
                    }
                });
        handlePermission(
                Manifest.permission.INTERNET,
                PERMISSION_REQUEST_CODE_INTERNET,
                getString(R.string.internet_required),
                new Runnable() {
                    @Override
                    public void run() {
                        setupAfterPermissionInternet();
                    }
                });

        setupActionBarAndDrawer();
        if (savedInstanceState == null) {
            setMusicFragment(false);
        }

        new PlayBackIntentBuilder(this)
                .setup()
                .buildAndSubmit();
    }

    private void handlePermission(@NonNull String permission, int requestCode, String deniedUserMessage, Runnable runnable) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            runnable.run();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, deniedUserMessage, Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        }
    }

    private void setupAfterPermissionReadExternalStorage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.key_first_app_execution), true)) {
            this.musicDao.afterInstallationSetup();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.key_first_app_execution), false);
            editor.apply();
        }

        final Intent intent = new Intent(this, FileScannerService.class);
        startService(intent);
    }

    private void setupAfterPermissionInternet() {
        Intent intent = new Intent(this, AlbumInfoFetcherService.class);
        startService(intent);

        intent = new Intent(this, ArtistInfoFetcherService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupAfterPermissionReadExternalStorage();
                } else {
                    Toast.makeText(this, getString(R.string.read_external_storage_required), Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_INTERNET:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupAfterPermissionInternet();
                } else {
                    Toast.makeText(this, getString(R.string.internet_required), Toast.LENGTH_LONG).show();
                }
        }
    }

    private void setupActionBarAndDrawer() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setMusicFragment(boolean replacePreviousFragment) {
        Fragment musicFragment = MusicFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (replacePreviousFragment) {
            transaction.replace(R.id.fragment_container, musicFragment);
            transaction.addToBackStack(null);
        } else {
            transaction.add(R.id.fragment_container, musicFragment);
        }

        transaction.commit();
    }

    private void setSettingsFragment() {
        Fragment settingsFragment = SettingsFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, settingsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setNowPlayingFragment(Song song, PlayStatus playStatus) {
        final FragmentManager manager = getSupportFragmentManager();
        NowPlayingFragment nowPlayingFragment = (NowPlayingFragment) manager.findFragmentById(R.id.now_playing_bar_container);
        if (nowPlayingFragment == null) {
            nowPlayingFragment = NowPlayingFragment.newInstance();

            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.now_playing_bar_container, nowPlayingFragment);
            transaction.commit();

            // We have to execute the pending transactions since we're about to update the new fragment's view...
            manager.executePendingTransactions();
        }

        nowPlayingFragment.update(song, playStatus);
    }

    private void removeNowPlayingFragment() {
        final FragmentManager manager = getSupportFragmentManager();
        NowPlayingFragment nowPlayingFragment = (NowPlayingFragment) manager.findFragmentById(R.id.now_playing_bar_container);
        if (nowPlayingFragment != null) {
            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(nowPlayingFragment);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (currentMenuItem == null || currentMenuItem != item.getItemId()) {
            switch (item.getItemId()) {
                case R.id.nav_music_library:
                    setMusicFragment(true);
                    break;
                case R.id.nav_settings:
                    setSettingsFragment();
                    break;
                case R.id.nav_about:
                    // TODO
                    break;
                case R.id.nav_contact:
                    // TODO
                    break;
            }
            currentMenuItem = item.getItemId();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public MusicDao getMusicDao() {
        return this.musicDao;
    }

    @Override
    public void songClicked(Song song) {
        new PlayBackIntentBuilder(this)
                .setSong(song)
                .setPlayerCommand(PLAY)
                .buildAndSubmit();
    }

    @Override
    public void albumClicked(Album album) {
        Fragment albumFragment = AlbumFragment.newInstance(this, album.getId());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, albumFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void artistClicked(Artist artist) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof MusicFragment) {
            MusicFragment musicFragment = (MusicFragment) fragment;
            musicFragment.setCurrentFragment(SubFragmentType.ALBUMS);
            musicFragment.filterAlbums(artist);
        }
    }

    @Override
    public void onClearMusicCache() {
        Toast.makeText(this, "CLEARING", Toast.LENGTH_LONG).show();
        // TODO: clear songs
        // TODO: clear albums
        // TODO: clear artists
        // TODO: clear all images
        // TODO: clear directories ???

        // TODO: make sure broadcasts are happening

        // TODO: relaunch the file scanner service (mind that directories MAY have been cleared -->
        //       see questions above.
    }

    @Override
    public void onResume() {
        super.onResume();

        this.playBackStatusReceiver = new PlayBackStatusReceiver();
        IntentFilter filter = new IntentFilter(PBBT_PLAYER_STATUS_UPDATE.name());
        registerReceiver(this.playBackStatusReceiver, filter);

        this.headsetReceiver = new HeadsetReceiver();
        filter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(this.headsetReceiver, filter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(this.playBackStatusReceiver);
        unregisterReceiver(this.headsetReceiver);
        super.onPause();
    }

    private class PlayBackStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: runOnUiThread(new Runnable() {    ?
            final Song song = getMusicDao().getCurrentSong();
            final PlayStatus playStatus = getMusicDao().getCurrentPlayStatus();
            if (playStatus == STOPPED) {
                removeNowPlayingFragment();
            } else {
                setNowPlayingFragment(song, playStatus);
            }
        }
    }

    private class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(AudioManager.ACTION_HEADSET_PLUG)) {
                boolean isPluggedIn = intent.getIntExtra("state", 0) == 1;
                if (!isPluggedIn) {
                    new PlayBackIntentBuilder(MainActivity.this)
                            .setPlayerCommand(PAUSE)
                            .buildAndSubmit();
                }
            }
        }
    }
}
