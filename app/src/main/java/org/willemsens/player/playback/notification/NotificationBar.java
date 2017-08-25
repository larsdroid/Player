package org.willemsens.player.playback.notification;

import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;
import org.willemsens.player.playback.PlayStatus;

abstract class NotificationBar extends RemoteViews {
    NotificationBar(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public void update(Song song, PlayStatus playStatus) {
        this.setImageViewBitmap(R.id.notification_bar_image, BitmapFactory.decodeByteArray(
                song.getAlbum().getImage().getImageData(), 0, song.getAlbum().getImage().getImageData().length));
        this.setTextViewText(R.id.notification_bar_track, String.valueOf(song.getTrack()));
        this.setTextViewText(R.id.notification_bar_song, song.getName());
        this.setTextViewText(R.id.notification_bar_album, song.getAlbum().getName());

        switch (playStatus) {
            case PAUSED:
                this.setImageViewResource(R.id.button_play_pause_stop, R.drawable.ic_play_arrow_black_48dp);
                break;
            case PLAYING:
                this.setImageViewResource(R.id.button_play_pause_stop, R.drawable.ic_pause_black_48dp);
                break;
            case STOPPED:
                this.setImageViewResource(R.id.button_play_pause_stop, R.drawable.ic_play_arrow_black_48dp);
        }
    }
}
