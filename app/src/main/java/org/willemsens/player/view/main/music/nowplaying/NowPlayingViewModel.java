package org.willemsens.player.view.main.music.nowplaying;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.helpers.SongWithAlbumInfo;
import org.willemsens.player.playback.PlayStatus;

public class NowPlayingViewModel extends AndroidViewModel {
    public final LiveData<SongWithAlbumInfo> songLiveData;
    public final LiveData<PlayStatus> playStatusLiveData;

    public NowPlayingViewModel(@NonNull Application application) {
        super(application);

        final AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        final MusicDao musicDao = appDatabase.musicDao();

        this.songLiveData = musicDao.getCurrentSongWithAlbumInfo();
        this.playStatusLiveData = musicDao.getCurrentPlayStatus();
    }
}
