package org.willemsens.player.persistence.entities.helpers;

public class AlbumWithImageAndArtist {
    public final long id;
    public final String name;
    public final Integer yearReleased;
    public final Integer length;
    public final byte[] imageData;
    public final long artistId;
    public final String artistName;

    public AlbumWithImageAndArtist(long id, String name, Integer yearReleased, Integer length, byte[] imageData, long artistId, String artistName) {
        this.id = id;
        this.name = name;
        this.yearReleased = yearReleased;
        this.length = length;
        this.imageData = imageData;
        this.artistId = artistId;
        this.artistName = artistName;
    }
}
