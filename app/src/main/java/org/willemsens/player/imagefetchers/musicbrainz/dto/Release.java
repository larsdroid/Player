package org.willemsens.player.imagefetchers.musicbrainz.dto;

public class Release {
    private String id;
    private String date;

    public Release() {
    }

    public String getId() {
        return id;
    }

    Integer getYear() {
        try {
            return date == null ? null : Integer.parseInt(date.substring(0, 4));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }
}
