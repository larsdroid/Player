package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
abstract class AbstractArtist implements Comparable<Artist> {
    @Key
    @Generated
    Long id;

    @Column(unique = true, nullable = false)
    String name;

    @Column
    InfoSource source;

    @ManyToOne
    Image image;

    @Override
    public int compareTo(@NonNull Artist b) {
        final Artist a = (Artist)this;
        return a.getName().compareTo(b.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist)) return false;

        final Artist a = (Artist)this;
        final Artist b = (Artist)o;

        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }

        return a.getName().equals(b.getName());
    }

    @Override
    public int hashCode() {
        final Artist a = (Artist)this;
        return a.getName().hashCode();
    }
}
