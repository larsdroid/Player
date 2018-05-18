package org.willemsens.player.view.main.music.songs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.helpers.SongWithAlbumInfo;

import java.util.List;

public class SongsViewModel extends AndroidViewModel {
    public final LiveData<List<SongWithAlbumInfo>> songsLiveData;
    public final LiveData<List<Album>> albumsLiveData; // For the "filter by album" list
    public final LiveData<List<Artist>> artistsLiveData; // For the "filter by artist" list

    public SongsViewModel(@NonNull Application application) {
        super(application);

        final AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        final MusicDao musicDao = appDatabase.musicDao();

        this.songsLiveData = musicDao.getAllSongsWithAlbumInfo();
        this.albumsLiveData = musicDao.getAllAlbums();
        this.artistsLiveData = musicDao.getAllArtists();
    }
}
