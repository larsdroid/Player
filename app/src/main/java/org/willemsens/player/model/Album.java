package org.willemsens.player.model;

public class Album {
    private final long id;
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
}
