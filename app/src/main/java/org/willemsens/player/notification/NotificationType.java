package org.willemsens.player.notification;

public enum NotificationType {
    MUSIC_PLAYING(1);

    private final int code;

    NotificationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
