package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
abstract class AbstractArtist implements Comparable<AbstractArtist> {
    @Key
    @Generated
    Long id;

    @Column(unique = true, nullable = false)
    String name;

    @ManyToOne
    Image image;

    @Override
    public int compareTo(@NonNull AbstractArtist that) {
        return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractArtist)) return false;

        AbstractArtist artist = (AbstractArtist) o;

        if (this.id != null && artist.id != null) {
            return this.id.equals(artist.id);
        }

        return name.equals(artist.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
