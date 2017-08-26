package org.willemsens.player.playback;

import android.content.Context;
import android.content.Intent;

import org.willemsens.player.R;
import org.willemsens.player.model.Song;

public class PlayBackIntentBuilder {
    private final Context context;
    private Intent intent;

    public PlayBackIntentBuilder(Context context) {
        this.context = context;
        this.intent = new Intent(context, PlayBackService.class);
    }

    public PlayBackIntentBuilder setSong(Song song) {
        this.intent.putExtra(context.getString(R.string.key_song_id), song.getId());
        return this;
    }

    public PlayBackIntentBuilder setPlayCommand(PlayCommand playCommand) {
        this.intent.putExtra(context.getString(R.string.key_play_command), playCommand.name());
        return this;
    }

    public Intent build() {
        final Intent returnIntent = this.intent;
        this.intent = new Intent(context, PlayBackService.class);
        return returnIntent;
    }

    public PlayBackIntentBuilder buildAndSubmit() {
        this.context.startService(build());
        return this;
    }
}
