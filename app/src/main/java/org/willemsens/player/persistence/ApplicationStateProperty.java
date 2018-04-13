package org.willemsens.player.persistence;

import android.content.Context;
import android.support.annotation.StringRes;
import org.willemsens.player.R;

public enum ApplicationStateProperty {
    CURRENT_PLAY_STATUS(R.string.key_current_play_status),
    CURRENT_PLAY_MODE(R.string.key_current_play_mode),
    CURRENT_MILLIS(R.string.key_current_millis),
    CURRENT_SONG_ID(R.string.key_current_song_id);

    @StringRes
    private final int keyResourceId;

    ApplicationStateProperty(@StringRes int keyResourceId) {
        this.keyResourceId = keyResourceId;
    }

    public String getString(Context context) {
        return context.getString(this.keyResourceId);
    }
}
