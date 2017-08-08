package org.willemsens.player.imagefetchers.musicbrainz.dto;

public class ImagesReponse {
    private Image[] images;

    public ImagesReponse() {
    }

    /*public String getFirstImage() {
        return images != null && images.length > 0 && images[0] != null ? images[0].getImage() : null;
    }

    public String getFirstSmallThumbnail() {
        return images != null && images.length > 0 && images[0] != null ? images[0].getThumbnails().getSmall() : null;
    }*/

    public String getFirstLargeThumbnail() {
        return images != null && images.length > 0 && images[0] != null ? images[0].getThumbnails().getLarge() : null;
    }
}
