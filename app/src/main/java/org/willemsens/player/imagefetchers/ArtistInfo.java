package org.willemsens.player.imagefetchers;

import org.willemsens.player.model.InfoSource;

public class ArtistInfo extends FetchedInfo {
    private final String imageUrl;

    public ArtistInfo(InfoSource infoSource, String imageUrl) {
        super(infoSource);
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
