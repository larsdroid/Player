package org.willemsens.player.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Objects;

@Entity(foreignKeys = {
        @ForeignKey(entity = Image.class,
                parentColumns = "id",
                childColumns = "imageId")},
        indices = {
                @Index(value = {"name"},
                        unique = true)
        })
public class Artist implements Comparable<Artist> {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    public Integer imageId;

    public Artist(@NonNull String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NonNull Artist that) {
        return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Artist && this.id == ((Artist) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
