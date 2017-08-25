package org.willemsens.player.fetchers.discogs.dto;

public class Release {
    private long id;
    private String year;

    public Release() {
    }

    public long getId() {
        return id;
    }

    Integer getYear() {
        try {
            return year == null ? null : Integer.parseInt(year);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
