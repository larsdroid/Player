package org.willemsens.player.playback.notification;

public enum NotificationType {
    MUSIC_PLAYING(1, "MUSIC_PLAY_CHANNEL");

    private final int code;
    private final String channel;

    NotificationType(int code, String channel) {
        this.code = code;
        this.channel = channel;
    }

    public int getCode() {
        return this.code;
    }

    public String getChannel() {
        return channel;
    }
}
