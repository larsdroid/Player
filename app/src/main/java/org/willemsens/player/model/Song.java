package org.willemsens.player.model;

import android.support.annotation.NonNull;

public class Song implements Comparable<Song> {
    private final Long id;
    private final String name;
    private Artist artist;
    private Album album;
    private final int length; // In seconds
    private final String file;

    public Song(long id, String name, Artist artist, Album album, int length, String file) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.file = file;
    }

    public Song(String name, Artist artist, Album album, int length, String file) {
        this.id = null;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album getAlbum() {
        return album;
    }

    public int getLength() {
        return length;
    }

    public String getFile() {
        return file;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artist=" + artist +
                ", album=" + album +
                ", length=" + length +
                '}';
    }

    @Override
    public int compareTo(@NonNull Song that) {
        return this.file.compareTo(that.file);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;

        Song song = (Song) o;

        return file.equals(song.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
