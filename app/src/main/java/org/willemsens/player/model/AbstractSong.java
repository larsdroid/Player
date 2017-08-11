package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class AbstractSong implements Comparable<AbstractSong> {
    @Key
    @Generated
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToOne
    @Column(nullable = false)
    Artist artist;

    @ManyToOne
    Album album;

    @Column(nullable = false)
    int length; // In seconds

    @Column(unique = true, nullable = false)
    String file;

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
