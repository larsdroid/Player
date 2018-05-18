package org.willemsens.player.musiclibrary;

public enum MusicLibraryBroadcastType {
    MLBT_ARTIST_INSERTED,
    MLBT_ARTISTS_INSERTED,
    MLBT_ARTISTS_DELETED,
    MLBT_ALBUM_INSERTED,
    MLBT_ALBUMS_INSERTED,
    MLBT_ALBUM_UPDATED,
    MLBT_ALBUMS_DELETED,
    MLBT_SONG_INSERTED,
    MLBT_SONGS_INSERTED,
    MLBT_SONG_UPDATED,
    MLBT_SONGS_DELETED
}
/*
filter.addAction(MLBT_SONGS_INSERTED.name());
        filter.addAction(MLBT_SONG_INSERTED.name());
        filter.addAction(MLBT_SONG_UPDATED.name());
        filter.addAction(MLBT_SONGS_DELETED.name());
        filter.addAction(MLBT_ARTISTS_INSERTED.name());
        filter.addAction(MLBT_ARTIST_INSERTED.name());
        filter.addAction(MLBT_ARTISTS_DELETED.name());
        filter.addAction(MLBT_ALBUMS_INSERTED.name());
        filter.addAction(MLBT_ALBUM_INSERTED.name());
        filter.addAction(MLBT_ALBUM_UPDATED.name());
        filter.addAction(MLBT_ALBUMS_DELETED.name());
*/