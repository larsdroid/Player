package org.willemsens.player.view.main.album;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;

import java.util.List;

public class AlbumAndSongsViewModel extends ViewModel {
    public final LiveData<Album> albumLiveData;
    public final LiveData<Artist> artistLiveData;
    public final LiveData<List<Song>> songsLiveData;
    public final LiveData<Image> coverArtLiveData;

    public AlbumAndSongsViewModel(Context context, long albumId) {
        final AppDatabase appDatabase = AppDatabase.getAppDatabase(context);
        final MusicDao musicDao = appDatabase.musicDao();

        this.albumLiveData = musicDao.getAlbum(albumId);
        this.artistLiveData = musicDao.getArtistForAlbum(albumId);
        this.songsLiveData = musicDao.getAllSongs(albumId);
        this.coverArtLiveData = musicDao.getImageForAlbum(albumId);
    }
}
