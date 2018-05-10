package org.willemsens.player.util;

public class StringFormat {
    public static String formatToSongLength(Integer songLengthSeconds) {
        if (songLengthSeconds != null) {
            return String.format("%d:%02d", songLengthSeconds / 60, songLengthSeconds % 60);
        } else {
            return "--:--";
        }
    }
}
