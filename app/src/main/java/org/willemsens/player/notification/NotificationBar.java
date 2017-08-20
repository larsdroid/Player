package org.willemsens.player.notification;

import android.widget.RemoteViews;

import org.willemsens.player.R;

public class NotificationBar extends RemoteViews {
    public NotificationBar(String packageName) {
        super(packageName, R.layout.notification_bar);
        this.setTextViewText(R.id.test_1, "JOS_THE_BOSS");
    }
}
