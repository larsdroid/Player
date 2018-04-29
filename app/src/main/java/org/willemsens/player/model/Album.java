package org.willemsens.player.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
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
})
public class Album {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    public int artistId;
    public Integer yearReleased;
    public Integer length;
    public Integer imageId;

    public Album(@NonNull String name, int artistId) {
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
