package org.willemsens.player.imagefetchers.discogs.dto;

public class ReleasesResponse {
    private Release[] results;

    public ReleasesResponse() {
    }

    public Release[] getReleases() {
        return results;
    }
}
