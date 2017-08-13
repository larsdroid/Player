package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
abstract class AbstractAlbum implements Comparable<Album> {
    @Key
    @Generated
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToOne
    @Column(nullable = false)
    Artist artist;

    Integer yearReleased;

    @Column(nullable = false)
    int length;

    @Column
    InfoSource source;

    @ManyToOne
    Image image;

    @Override
    public int compareTo(@NonNull Album b) {
        final Album a = (Album)this;
        if (!a.getArtist().equals(b.getArtist())) {
            return a.getArtist().compareTo(b.getArtist());
        } else {
            if (a.getYearReleased() != null && b.getYearReleased() != null
                    && !a.getYearReleased().equals(b.getYearReleased())) {
                return a.getYearReleased() - b.getYearReleased();
            } else {
                return a.getName().compareTo(b.getName());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;

        final Album a = (Album)this;
        final Album b = (Album)o;

        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }

        return a.getName().equals(b.getName()) && a.getArtist().equals(b.getArtist());
    }

    @Override
    public int hashCode() {
        final Album a = (Album)this;
        int result = a.getName().hashCode();
        result = 31 * result + a.getArtist().hashCode();
        return result;
    }
}
