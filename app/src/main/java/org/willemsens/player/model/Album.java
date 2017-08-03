package org.willemsens.player.model;

import android.support.annotation.NonNull;

public class Album implements Comparable<Album> {
    private final Long id;
    private final String name;
    private final Artist artist;
    private final int year;
    private final int length;

    public Album(long id, String name, Artist artist, int year, int length) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.year = year;
        this.length = length;
    }

    public Album(String name, Artist artist, int year, int length) {
        this.id = null;
        this.name = name;
        this.artist = artist;
        this.year = year;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public Artist getArtist() {
        return artist;
    }

    public int getYear() {
        return year;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artist=" + artist +
                ", year=" + year +
                ", length=" + length +
                '}';
    }

    @Override
    public int compareTo(@NonNull Album that) {
        if (this.artist.equals(that.artist)) {
            return this.artist.compareTo(that.artist);
        } else {
            return this.name.compareTo(that.name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;

        Album album = (Album) o;

        return name.equals(album.name) && artist.equals(album.artist);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + artist.hashCode();
        return result;
    }
}
