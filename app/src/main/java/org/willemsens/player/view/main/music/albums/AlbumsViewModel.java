package org.willemsens.player.view.main.music.albums;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.helpers.AlbumWithImageAndArtist;

import java.util.List;

public class AlbumsViewModel extends AndroidViewModel {
    public final LiveData<List<AlbumWithImageAndArtist>> albumsLiveData;
    public final LiveData<List<Artist>> artistsLiveData; // For the "filter by artist" list

    public AlbumsViewModel(@NonNull Application application) {
        super(application);

        final AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        final MusicDao musicDao = appDatabase.musicDao();

        this.albumsLiveData = musicDao.getAllAlbumsWithImages();
        this.artistsLiveData = musicDao.getAllArtists();
    }
}
