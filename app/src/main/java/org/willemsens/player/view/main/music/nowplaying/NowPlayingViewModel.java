package org.willemsens.player.view.main.music.nowplaying;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.helpers.SongWithAlbumInfo;

public class NowPlayingViewModel extends AndroidViewModel {
    public final LiveData<SongWithAlbumInfo> songLiveData;

    public NowPlayingViewModel(@NonNull Application application) {
        super(application);

        final AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        final MusicDao musicDao = appDatabase.musicDao();

        this.songLiveData = musicDao.getCurrentSongWithAlbumInfo();
    }
}
