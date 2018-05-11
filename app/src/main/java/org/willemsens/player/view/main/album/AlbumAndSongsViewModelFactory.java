package org.willemsens.player.view.main.album;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

public class AlbumAndSongsViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Context context;
    private final long albumId;

    public AlbumAndSongsViewModelFactory(Context context, long albumId) {
        this.context = context;
        this.albumId = albumId;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AlbumAndSongsViewModel(context, albumId);
    }
}