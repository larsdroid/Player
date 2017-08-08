package org.willemsens.player.imagefetchers.musicbrainz.dto;

public class ArtistsResponse {
    private Artist[] artists;

    public ArtistsResponse() {
    }

    public String getFirstArtistID() {
        return artists != null && artists.length > 0 && artists[0] != null ? this.artists[0].getId() : null;
    }
}
