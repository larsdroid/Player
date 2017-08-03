package org.willemsens.player.model;

import android.support.annotation.NonNull;

public class Artist implements Comparable<Artist> {
    private final Long id;
    private final String name;

    public Artist(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Artist(String name) {
        this.id = null;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(@NonNull Artist that) {
        return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist)) return false;

        Artist artist = (Artist) o;

        return name.equals(artist.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
