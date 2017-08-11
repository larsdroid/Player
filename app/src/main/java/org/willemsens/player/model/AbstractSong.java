package org.willemsens.player.model;

import android.support.annotation.NonNull;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class AbstractSong implements Comparable<AbstractSong> {
    @Key
    @Generated
    Long id;

    String name;

    @ManyToOne
    Artist artist;

    @ManyToOne
    Album album;

    int length; // In seconds
    String file;

    @Override
    public String toString() {
        return "AbstractSong{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artist=" + artist +
                ", album=" + album +
                ", length=" + length +
                ", file=" + file +
                '}';
    }

    @Override
    public int compareTo(@NonNull AbstractSong that) {
        return this.file.compareTo(that.file);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSong)) return false;

        AbstractSong song = (AbstractSong) o;

        if (this.id != null && song.id != null) {
            return this.id.equals(song.id);
        }

        return file.equals(song.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
