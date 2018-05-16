package org.willemsens.player.persistence.entities.helpers;

public class SongWithAlbumInfo {
    public final long id;
    public final String name;
    public final int track;
    public final Integer length;
    public final String albumName;
    public final byte[] albumImageData;
    public final String artistName;

    public SongWithAlbumInfo(long id, String name, int track, Integer length, String albumName, byte[] albumImageData, String artistName) {
        this.id = id;
        this.name = name;
        this.track = track;
        this.length = length;
        this.albumName = albumName;
        this.albumImageData = albumImageData;
        this.artistName = artistName;
    }
}
