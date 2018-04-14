package org.willemsens.player.playback;

import android.content.Context;
import android.content.Intent;
import org.willemsens.player.model.Song;

import static org.willemsens.player.playback.PlayBackIntentPayloadType.PLAYBACK_PAYLOAD_PLAYER_COMMAND;
import static org.willemsens.player.playback.PlayBackIntentPayloadType.PLAYBACK_PAYLOAD_PLAY_MODE;
import static org.willemsens.player.playback.PlayBackIntentPayloadType.PLAYBACK_PAYLOAD_SONG_ID;
import static org.willemsens.player.playback.PlayBackIntentType.PLAYBACK_DISMISS;
import static org.willemsens.player.playback.PlayBackIntentType.PLAYBACK_PLAYER_COMMAND;
import static org.willemsens.player.playback.PlayBackIntentType.PLAYBACK_SETUP;
import static org.willemsens.player.playback.PlayBackIntentType.PLAYBACK_SET_PLAY_MODE;
import static org.willemsens.player.playback.PlayBackIntentType.PLAYBACK_SET_SONG_ID;

/**
 * An Intent builder for creating Intents that are targeted AT the PlayBackService (not coming
 * FROM the PlayBackService).
 */
public class PlayBackIntentBuilder {
    private final Context context;
    private Intent intent;

    public PlayBackIntentBuilder(Context context) {
        this.context = context;
        this.intent = new Intent(context, PlayBackService.class);
    }

    public PlayBackIntentBuilder setSong(Song song) {
        this.intent.setAction(PLAYBACK_SET_SONG_ID.getString(context));
        this.intent.putExtra(PLAYBACK_PAYLOAD_SONG_ID.getString(context), song.getId());
        return this;
    }

    public PlayBackIntentBuilder setPlayerCommand(PlayerCommand playerCommand) {
        if (this.intent.getAction() == null) {
            this.intent.setAction(PLAYBACK_PLAYER_COMMAND.getString(context));
        }
        this.intent.putExtra(PLAYBACK_PAYLOAD_PLAYER_COMMAND.getString(context), playerCommand.name());
        return this;
    }

    public PlayBackIntentBuilder setPlayMode(PlayMode playMode) {
        this.intent.setAction(PLAYBACK_SET_PLAY_MODE.getString(context));
        this.intent.putExtra(PLAYBACK_PAYLOAD_PLAY_MODE.getString(context), playMode.name());
        return this;
    }

    public PlayBackIntentBuilder setup() {
        this.intent.setAction(PLAYBACK_SETUP.getString(context));
        return this;
    }

    public PlayBackIntentBuilder dismiss() {
        this.intent.setAction(PLAYBACK_DISMISS.getString(context));
        return this;
    }

    public Intent build() {
        final Intent returnIntent = this.intent;
        this.intent = new Intent(context, PlayBackService.class);
        return returnIntent;
    }

    public void buildAndSubmit() {
        this.context.startService(build());
    }
}
