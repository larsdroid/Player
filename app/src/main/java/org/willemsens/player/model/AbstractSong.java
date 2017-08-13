package org.willemsens.player.model;

import android.support.annotation.NonNull;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
abstract class AbstractSong implements Comparable<AbstractSong> {
    @Key
    @Generated
    Long id;

    @Column(nullable = false)
    String name;

    // Artist is being fetched eagerly in MusicDao. Hoping for eager fetching support in requery.
    @ManyToOne
    @Column(nullable = false)
    Artist artist;

    // Album is being fetched eagerly in MusicDao. Hoping for eager fetching support in requery.
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
    public int compareTo(@NonNull AbstractSong that) {
        if (this.album.equals(that.album)) {
            return this.track - that.track;
        } else {
            return this.album.compareTo(that.album);
        }
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
