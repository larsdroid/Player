package org.willemsens.player.notification;

import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;

abstract class NotificationBar extends RemoteViews {
    NotificationBar(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public void setSong(Song song) {
        this.setImageViewBitmap(R.id.notification_bar_image, BitmapFactory.decodeByteArray(
                song.getAlbum().getImage().getImageData(), 0, song.getAlbum().getImage().getImageData().length));
        this.setTextViewText(R.id.notification_bar_track, String.valueOf(song.getTrack()));
        this.setTextViewText(R.id.notification_bar_song, song.getName());
        this.setTextViewText(R.id.notification_bar_album, song.getAlbum().getName());
    }
}
