package org.willemsens.player.playback;

import android.content.Context;
import android.content.Intent;
import org.willemsens.player.model.Song;

import static org.willemsens.player.playback.PlayBackIntentPayloadType.PBIPT_PLAYER_COMMAND;
import static org.willemsens.player.playback.PlayBackIntentPayloadType.PBIPT_PLAY_MODE;
import static org.willemsens.player.playback.PlayBackIntentPayloadType.PBIPT_SONG_ID;
import static org.willemsens.player.playback.PlayBackIntentType.PBIT_DISMISS;
import static org.willemsens.player.playback.PlayBackIntentType.PBIT_PLAYER_COMMAND;
import static org.willemsens.player.playback.PlayBackIntentType.PBIT_SETUP;
import static org.willemsens.player.playback.PlayBackIntentType.PBIT_SET_PLAY_MODE;
import static org.willemsens.player.playback.PlayBackIntentType.PBIT_SET_SONG_ID;

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
        this.intent.setAction(PBIT_SET_SONG_ID.name());
        this.intent.putExtra(PBIPT_SONG_ID.name(), song.getId());
        return this;
    }

    public PlayBackIntentBuilder setPlayerCommand(PlayerCommand playerCommand) {
        if (this.intent.getAction() == null) {
            this.intent.setAction(PBIT_PLAYER_COMMAND.name());
        }
        this.intent.putExtra(PBIPT_PLAYER_COMMAND.name(), playerCommand.name());
        return this;
    }

    public PlayBackIntentBuilder setPlayMode(PlayMode playMode) {
        this.intent.setAction(PBIT_SET_PLAY_MODE.name());
        this.intent.putExtra(PBIPT_PLAY_MODE.name(), playMode.name());
        return this;
    }

    public PlayBackIntentBuilder setup() {
        this.intent.setAction(PBIT_SETUP.name());
        return this;
    }

    public PlayBackIntentBuilder dismiss() {
        this.intent.setAction(PBIT_DISMISS.name());
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
