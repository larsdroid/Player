package org.willemsens.player.persistence.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Image {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String url;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] imageData;

    public Image(byte[] imageData) {
        this.imageData = imageData;
    }
}
