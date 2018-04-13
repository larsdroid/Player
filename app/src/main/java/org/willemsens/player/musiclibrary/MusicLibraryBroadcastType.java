package org.willemsens.player.musiclibrary;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import org.willemsens.player.R;

public enum MusicLibraryBroadcastType {
    ARTIST_INSERTED(R.string.key_artist_inserted),
    ARTISTS_INSERTED(R.string.key_artists_inserted),
    ARTIST_UPDATED(R.string.key_artist_updated),
    ALBUM_INSERTED(R.string.key_album_inserted),
    ALBUMS_INSERTED(R.string.key_albums_inserted),
    ALBUM_UPDATED(R.string.key_album_updated),
    SONG_INSERTED(R.string.key_song_inserted),
    SONGS_INSERTED(R.string.key_songs_inserted);

    @StringRes
    private int intentActionResourceId;

    MusicLibraryBroadcastType(@StringRes int intentActionResourceId) {
        this.intentActionResourceId = intentActionResourceId;
    }

    public String getString(Context context) {
        return context.getString(this.intentActionResourceId);
    }

    public String getString(Fragment fragment) {
        return fragment.getString(this.intentActionResourceId);
    }
}
