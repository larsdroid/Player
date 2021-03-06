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
        @ForeignKey(entity = Album.class,
                parentColumns = "id",
                childColumns = "albumId")},
        indices = {
                @Index(value = {"file"},
                        unique = true),
                @Index(value = {"artistId"}),
                @Index(value = {"albumId"})
        })
public class Song {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public long artistId;
    public long albumId;
    public int track;
    public Integer length; // In seconds

    @NonNull
    public String file;

    public Song(@NonNull String name, long artistId, long albumId, int track, @NonNull String file) {
        this.name = name;
        this.artistId = artistId;
        this.albumId = albumId;
        this.track = track;
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Song && this.id == ((Song) o).id;

    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        return file;
    }
}
