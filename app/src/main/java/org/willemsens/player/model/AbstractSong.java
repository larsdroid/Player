package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
abstract class AbstractSong implements Comparable<Song> {
    @Key
    @Generated
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToOne
    @Column(nullable = false)
    Artist artist;

    @ManyToOne
    @Column(nullable = false)
    Album album;

    @Column(nullable = false)
    int track;

    @Column(nullable = false)
    int length; // In seconds

    @Column(unique = true, nullable = false)
    String file;

    @Override
    public int compareTo(@NonNull Song b) {
        final Song a = (Song)this;
        if (a.getAlbum().equals(b.getAlbum())) {
            return a.getTrack() - b.getTrack();
        } else {
            return a.getAlbum().compareTo(b.getAlbum());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;

        final Song a = (Song)this;
        final Song b = (Song)o;

        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }

        return a.getFile().equals(b.getFile());

    }

    @Override
    public int hashCode() {
        final Song a = (Song)this;
        return a.getFile().hashCode();
    }
}
