package org.willemsens.player.imagefetchers.musicbrainz.dto;

public class ReleasesResponse {
    private Release[] releases;

    public ReleasesResponse() {
    }

    public Release[] getReleases() {
        return releases;
    }

    public Integer getOldestReleaseYear() {
        Integer year = null;
        for (Release release : releases) {
            if (year == null || (release.getYear() != null && release.getYear() < year)) {
                year = release.getYear();
            }
        }
        return year;
    }
}
