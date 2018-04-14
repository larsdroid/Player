package org.willemsens.player.playback;

import android.content.Context;
import android.support.annotation.StringRes;
import org.willemsens.player.R;

public enum PlayBackBroadcastType {
    PLAYER_STATUS_UPDATE(R.string.key_player_status_update);

    @StringRes
    private final int stringResId;

    PlayBackBroadcastType(@StringRes int stringResId) {
        this.stringResId = stringResId;
    }

    public String getString(Context context) {
        return context.getString(this.stringResId);
    }
}
