package org.willemsens.player.persistence;

import android.provider.BaseColumns;

class MusicContract {
    private MusicContract() {
    }

    static class ArtistEntry implements BaseColumns {
        static final String TABLE_NAME = "artist";
        static final String COLUMN_NAME_NAME = "name";

        static final String[] ALL_COLUMNS = { _ID, COLUMN_NAME_NAME };
    }

    static class AlbumEntry implements BaseColumns {
        static final String TABLE_NAME = "album";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_ARTIST = "artist";
        static final String COLUMN_NAME_YEAR = "year";
        static final String COLUMN_NAME_LENGTH = "length";

        static final String[] ALL_COLUMNS = { _ID, COLUMN_NAME_NAME, COLUMN_NAME_ARTIST, COLUMN_NAME_YEAR, COLUMN_NAME_LENGTH };
    }

    static class SongEntry implements BaseColumns {
        static final String TABLE_NAME = "song";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_ARTIST = "artist";
        static final String COLUMN_NAME_ALBUM = "album";
        static final String COLUMN_NAME_LENGTH = "length";
        static final String COLUMN_NAME_FILE = "file";

        static final String[] ALL_COLUMNS = { _ID, COLUMN_NAME_NAME, COLUMN_NAME_ARTIST, COLUMN_NAME_ALBUM, COLUMN_NAME_LENGTH, COLUMN_NAME_FILE };
    }

    static class DirectoryEntry implements BaseColumns {
        static final String TABLE_NAME = "directory";
        static final String COLUMN_NAME_PATH = "path";
        static final String COLUMN_NAME_SCAN_TIMESTAMP = "scan_timestamp";

        static final String[] ALL_COLUMNS = { _ID, COLUMN_NAME_PATH, COLUMN_NAME_SCAN_TIMESTAMP };
    }
}
