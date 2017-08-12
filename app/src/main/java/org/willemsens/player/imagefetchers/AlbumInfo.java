package org.willemsens.player.imagefetchers;

import org.willemsens.player.model.InfoSource;

public class AlbumInfo extends FetchedInfo {
    private final String coverImageUrl;
    private final Integer year;

    public AlbumInfo(InfoSource infoSource, String coverImageUrl, Integer year) {
        super(infoSource);
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
