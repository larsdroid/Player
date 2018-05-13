package org.willemsens.player.view.main.music.artists;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import org.willemsens.player.persistence.AppDatabase;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.helpers.ArtistWithImage;

import java.util.List;

public class ArtistsViewModel extends AndroidViewModel {
    public final LiveData<List<ArtistWithImage>> artistWithImagesLiveData;

    public ArtistsViewModel(Application application) {
        super(application);

        final AppDatabase appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        final MusicDao musicDao = appDatabase.musicDao();

        this.artistWithImagesLiveData = musicDao.getAllArtistsWithImages();
    }
}
