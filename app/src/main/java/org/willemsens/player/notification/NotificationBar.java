package org.willemsens.player.notification;

import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import org.willemsens.player.R;
import org.willemsens.player.model.Song;

public class NotificationBar extends RemoteViews {
    public NotificationBar(String packageName) {
        super(packageName, R.layout.notification_bar);
    }

    public void setSong(Song song) {
        this.setImageViewBitmap(R.id.notification_bar_image, BitmapFactory.decodeByteArray(
                song.getAlbum().getImage().getImageData(), 0, song.getAlbum().getImage().getImageData().length));
        this.setTextViewText(R.id.notification_bar_track, String.valueOf(song.getTrack()));
        this.setTextViewText(R.id.notification_bar_song, song.getName());
        this.setTextViewText(R.id.notification_bar_album, song.getAlbum().getName());
    }
}
