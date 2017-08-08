package org.willemsens.player.imagefetchers.discogs.dto;

public class ArtistsResponse {
    private Artist[] results;

    public ArtistsResponse() {
    }

    public Long getFirstArtistID() {
        return results != null && results.length > 0 && results[0] != null ? results[0].getId() : null;
    }
}
