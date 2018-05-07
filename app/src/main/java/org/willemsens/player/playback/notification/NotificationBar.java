package org.willemsens.player.playback.notification;

import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import org.willemsens.player.R;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Image;
import org.willemsens.player.model.Song;
import org.willemsens.player.playback.PlayStatus;

abstract class NotificationBar extends RemoteViews {
    NotificationBar(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public void update(Song song, Album album, Image albumCover, PlayStatus playStatus) {
        this.setImageViewBitmap(R.id.notification_bar_image, BitmapFactory.decodeByteArray(
                albumCover.imageData, 0, albumCover.imageData.length));
        this.setTextViewText(R.id.notification_bar_track, String.valueOf(song.track));
        this.setTextViewText(R.id.notification_bar_song, song.name);
        this.setTextViewText(R.id.notification_bar_album, album.name);

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
