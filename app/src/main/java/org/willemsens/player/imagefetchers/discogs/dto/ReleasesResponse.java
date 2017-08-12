package org.willemsens.player.imagefetchers.discogs.dto;

public class ReleasesResponse {
    private Release[] results;

    public ReleasesResponse() {
    }

    public Release[] getReleases() {
        return results;
    }

    public Integer getOldestReleaseYear() {
        Integer year = null;
        for (Release release : results) {
            if (year == null || (release.getYear() != null && release.getYear() < year)) {
                year = release.getYear();
            }
        }
        return year;
    }
}
