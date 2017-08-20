package org.willemsens.player.notification;

public enum NotificationType {
    DEFAULT_START(1);

    private final int code;

    NotificationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
