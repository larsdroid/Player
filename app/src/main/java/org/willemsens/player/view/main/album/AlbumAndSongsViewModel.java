package org.willemsens.player.view.main.album;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Image;
import org.willemsens.player.persistence.entities.Song;

import java.util.List;

public class AlbumAndSongsViewModel extends AndroidViewModel {
    public final LiveData<Album> albumLiveData;
    public final LiveData<Artist> artistLiveData;
    public final LiveData<List<Song>> songsLiveData;
    public final LiveData<Image> coverArtLiveData;

    AlbumAndSongsViewModel(@NonNull Application application, long albumId) {
        super(application);

        final AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        final MusicDao musicDao = appDatabase.musicDao();

        this.albumLiveData = musicDao.getAlbum(albumId);
        this.artistLiveData = musicDao.getArtistForAlbum(albumId);
        this.songsLiveData = musicDao.getAllSongs(albumId);
        this.coverArtLiveData = musicDao.getImageForAlbum(albumId);
    }
}
