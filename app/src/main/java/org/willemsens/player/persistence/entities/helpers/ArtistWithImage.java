package org.willemsens.player.persistence.entities.helpers;

import android.arch.persistence.room.ColumnInfo;

public class ArtistWithImage {
    public final long id;
    public final String name;
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public final byte[] imageData;

    public ArtistWithImage(long id, String name, byte[] imageData) {
        this.id = id;
        this.name = name;
        this.imageData = imageData;
    }
}
