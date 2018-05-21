package org.willemsens.player.persistence.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Objects;

@Entity(foreignKeys = {
        @ForeignKey(entity = Artist.class,
                parentColumns = "id",
                childColumns = "artistId"),
        @ForeignKey(entity = Image.class,
                parentColumns = "id",
                childColumns = "imageId")
}, indices = {
        @Index(value = {"artistId"}),
        @Index(value = {"imageId"})
})
public class Album {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public long artistId;
    public Integer yearReleased;
    public Integer length;
    public Long imageId;

    public Integer currentTrack;
    public Integer currentMillisInTrack;
    public int playCount = 0;

    public Album(@NonNull String name, long artistId) {
        this.name = name;
        this.artistId = artistId;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Album && this.id == ((Album) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, artistId);
    }
}
