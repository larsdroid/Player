package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
abstract class AbstractAlbum implements Comparable<AbstractAlbum> {
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

    @ManyToOne
    Image image;

    @Override
    public int compareTo(@NonNull AbstractAlbum that) {
        if (this.artist.equals(that.artist)) {
            return this.artist.compareTo(that.artist);
        } else {
            return this.name.compareTo(that.name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractAlbum)) return false;

        AbstractAlbum album = (AbstractAlbum) o;

        if (this.id != null && album.id != null) {
            return this.id.equals(album.id);
        }

        return name.equals(album.name) && artist.equals(album.artist);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + artist.hashCode();
        return result;
    }
}
