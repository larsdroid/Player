package org.willemsens.player.persistence;

import android.provider.BaseColumns;

class MusicContract {
    private MusicContract() {
    }

    static class ArtistEntry implements BaseColumns {
        static final String TABLE_NAME = "artist";
        static final String COLUMN_NAME_NAME = "name";
    }

    static class AlbumEntry implements BaseColumns {
        static final String TABLE_NAME = "album";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_ARTIST = "artist";
        static final String COLUMN_NAME_YEAR = "year";
        static final String COLUMN_NAME_LENGTH = "length";
    }

    static class SongEntry implements BaseColumns {
        static final String TABLE_NAME = "song";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_ARTIST = "artist";
        static final String COLUMN_NAME_ALBUM = "album";
        static final String COLUMN_NAME_LENGTH = "length";
    }

    static class DirectoryEntry implements BaseColumns {
        static final String TABLE_NAME = "directory";
        static final String COLUMN_NAME_PATH = "path";
        static final String COLUMN_NAME_SCAN_TIMESTAMP = "scan_timestamp";
    }
}
