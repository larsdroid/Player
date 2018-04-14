package org.willemsens.player.playback;

import android.content.Context;
import android.support.annotation.StringRes;
import org.willemsens.player.R;

public enum PlayBackIntentPayloadType {
    PLAYBACK_PAYLOAD_PLAYER_COMMAND(R.string.key_player_command),
    PLAYBACK_PAYLOAD_PLAY_MODE(R.string.key_play_mode),
    PLAYBACK_PAYLOAD_SONG_ID(R.string.key_playback_song_id);

    @StringRes
    private final int keyStringResource;

    PlayBackIntentPayloadType(@StringRes int keyStringResource) {
        this.keyStringResource = keyStringResource;
    }

    public String getString(Context context) {
        return context.getString(this.keyStringResource);
    }
}
