package org.willemsens.player.musiclibrary;

import android.content.Context;
import android.support.annotation.StringRes;
import org.willemsens.player.R;

public enum MusicLibraryBroadcastPayloadType {
    ARTIST_ID(R.string.key_artist_id),
    ARTIST_IDS(R.string.key_artist_ids),
    ALBUM_ID(R.string.key_album_id),
    ALBUM_IDS(R.string.key_album_ids),
    SONG_ID(R.string.key_song_id);

    @StringRes
    private int intentExtraResourceId;

    MusicLibraryBroadcastPayloadType(@StringRes int intentExtraResourceId) {
        this.intentExtraResourceId = intentExtraResourceId;
    }

    public String getString(Context context) {
        return context.getString(this.intentExtraResourceId);
    }
}
