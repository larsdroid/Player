package org.willemsens.player.util;

public class StringFormat {
    public static String formatToSongLength(int songLengthSeconds) {
        return String.format("%d:%02d", songLengthSeconds / 60, songLengthSeconds % 60);
    }
}
