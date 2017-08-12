package org.willemsens.player.imagefetchers;

public class AlbumInfo {
    private final String coverImageUrl;
    private final Integer year;

    public AlbumInfo(String coverImageUrl, Integer year) {
        this.coverImageUrl = coverImageUrl;
        this.year = year;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public Integer getYear() {
        return year;
    }
}
