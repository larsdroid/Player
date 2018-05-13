package org.willemsens.player.view.main.album;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class AlbumAndSongsViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Application application;
    private final long albumId;

    AlbumAndSongsViewModelFactory(Application application, long albumId) {
        this.application = application;
        this.albumId = albumId;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AlbumAndSongsViewModel(application, albumId);
    }
}