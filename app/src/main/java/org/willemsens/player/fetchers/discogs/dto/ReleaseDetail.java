package org.willemsens.player.fetchers.discogs.dto;

public class ReleaseDetail {
    private ReleaseDetailImage[] images;

    public ReleaseDetail() {
    }

    public String getFirstImageURL() {
        return images != null && images.length > 0 && images[0] != null ? images[0].getUri() : null;
    }
}
