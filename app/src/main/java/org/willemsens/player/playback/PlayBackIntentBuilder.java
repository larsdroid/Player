package org.willemsens.player.playback;

import android.content.Context;
import android.content.Intent;

import org.willemsens.player.R;
import org.willemsens.player.model.Song;

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
        this.intent.setAction(context.getString(R.string.key_action_set_song_id));
        this.intent.putExtra(context.getString(R.string.key_playback_song_id), song.getId());
        return this;
    }

    public PlayBackIntentBuilder setPlayerCommand(PlayerCommand playerCommand) {
        if (this.intent.getAction() == null) {
            this.intent.setAction(context.getString(R.string.key_action_player_command));
        }
        this.intent.putExtra(context.getString(R.string.key_player_command), playerCommand.name());
        return this;
    }

    public PlayBackIntentBuilder setPlayMode(PlayMode playMode) {
        this.intent.setAction(context.getString(R.string.key_action_set_play_mode));
        this.intent.putExtra(context.getString(R.string.key_play_mode), playMode.name());
        return this;
    }

    public PlayBackIntentBuilder setup() {
        this.intent.setAction(context.getString(R.string.key_action_setup));
        return this;
    }

    public PlayBackIntentBuilder dismiss() {
        this.intent.setAction(context.getString(R.string.key_action_dismiss));
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
