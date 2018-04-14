package org.willemsens.player.playback;

import android.content.Context;
import android.support.annotation.StringRes;
import org.willemsens.player.R;

public enum PlayBackIntentType {
    PLAYBACK_SET_SONG_ID(R.string.key_action_set_song_id),
    PLAYBACK_SET_PLAY_MODE(R.string.key_action_set_play_mode),
    PLAYBACK_PLAYER_COMMAND(R.string.key_action_player_command),
    PLAYBACK_SETUP(R.string.key_action_setup),
    PLAYBACK_DISMISS(R.string.key_action_dismiss);

    @StringRes
    private final int actionKeyStringResource;

    PlayBackIntentType(@StringRes int actionKeyStringResource) {
        this.actionKeyStringResource = actionKeyStringResource;
    }

    public String getString(Context context) {
        return context.getString(this.actionKeyStringResource);
    }
}
