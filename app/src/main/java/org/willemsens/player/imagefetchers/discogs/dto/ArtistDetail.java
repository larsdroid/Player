package org.willemsens.player.imagefetchers.discogs.dto;

public class ArtistDetail {
    private ArtistDetailImage[] images;

    public ArtistDetail() {
    }

    public String getFirstImageURL() {
        return images != null && images.length > 0 && images[0] != null ? images[0].getUri() : null;
    }
}
