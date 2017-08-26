package org.willemsens.player.view.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.willemsens.player.PlayerApplication;
import org.willemsens.player.R;
import org.willemsens.player.fetchers.AlbumInfoFetcherService;
import org.willemsens.player.fetchers.ArtistInfoFetcherService;
import org.willemsens.player.filescanning.FileScannerService;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.playback.PlayBackIntentBuilder;
import org.willemsens.player.playback.PlayStatus;
import org.willemsens.player.view.DataAccessProvider;
import org.willemsens.player.view.nowplaying.NowPlayingFragment;
import org.willemsens.player.view.settings.SettingsFragment;
import org.willemsens.player.view.songs.OnSongClickedListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

import static org.willemsens.player.playback.PlayerCommand.PLAY;

public class MainActivity extends AppCompatActivity
        implements DataAccessProvider,
        OnSongClickedListener,
        NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CODE_INTERNET = 2;

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private Integer previousMenuItem;
    private MusicDao musicDao;
    private PlayBackStatusReceiver playBackStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final EntityDataStore<Persistable> dataStore = ((PlayerApplication) getApplication()).getData();
        this.musicDao = new MusicDao(dataStore);

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
            setMainFragment(false);
        }
    }

    private void handlePermission(@NonNull String permission, int requestCode, String deniedUserMessage, Runnable runnable) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            runnable.run();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, deniedUserMessage, Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ permission }, requestCode);
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

    private void setMainFragment(boolean replacePreviousFragment) {
        Fragment mainFragment = MainFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (replacePreviousFragment) {
            transaction.replace(R.id.fragment_container, mainFragment);
            transaction.addToBackStack(null);
        } else {
            transaction.add(R.id.fragment_container, mainFragment);
        }

        transaction.commit();
    }

    private void setSettingsFragment() {
        Fragment settingsFragment = SettingsFragment.newInstance("", "");
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, settingsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setNowPlayingFragment(Song song, PlayStatus playStatus) {
        final FragmentManager manager = getSupportFragmentManager();
        NowPlayingFragment nowPlayingFragment = (NowPlayingFragment)manager.findFragmentById(R.id.now_playing_bar_container);
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
        if (previousMenuItem == null || previousMenuItem != item.getItemId()) {
            switch (item.getItemId()) {
                case R.id.nav_music_library:
                    setMainFragment(true);
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
            previousMenuItem = item.getItemId();
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

    @Override
    public void onResume() {
        super.onResume();
        this.playBackStatusReceiver = new PlayBackStatusReceiver();
        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        final IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.key_player_status_changed));
        lbm.registerReceiver(this.playBackStatusReceiver, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.playBackStatusReceiver);
        super.onPause();
    }

    private class PlayBackStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String intentAction = intent.getAction();
            if (intentAction.equals(getString(R.string.key_player_status_changed))) {
                final long songId = intent.getLongExtra(getString(R.string.key_song_id), -1);
                final Song song = getMusicDao().findSong(songId);
                final PlayStatus playStatus = PlayStatus.valueOf(intent.getStringExtra(getString(R.string.key_play_status)));
                setNowPlayingFragment(song, playStatus);
            }
        }
    }
}
