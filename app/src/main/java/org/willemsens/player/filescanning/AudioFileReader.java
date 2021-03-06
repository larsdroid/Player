package org.willemsens.player.filescanning;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.willemsens.player.fetchers.AlbumInfoFetcherService;
import org.willemsens.player.fetchers.ArtistInfoFetcherService;
import org.willemsens.player.musiclibrary.MusicLibraryBroadcastBuilder;
import org.willemsens.player.persistence.MusicDao;
import org.willemsens.player.persistence.entities.Album;
import org.willemsens.player.persistence.entities.Artist;
import org.willemsens.player.persistence.entities.Song;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.willemsens.player.filescanning.Mp3ScanningPayloadType.MP3PT_SONG_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_SONG_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ALBUM_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_ARTIST_INSERTED;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastType.MLBT_SONG_INSERTED;

class AudioFileReader {
    private final MusicDao musicDao;
    private final Context context;

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    AudioFileReader(MusicDao musicDao, Context context) {
        this.musicDao = musicDao;
        this.context = context;
    }

    void readSong(File file) {
        try {
            final AudioFile audioFile = AudioFileIO.read(file);
            final Artist albumArtist = getAlbumArtist(audioFile, musicDao);
            final Album album = getAlbum(audioFile, musicDao, albumArtist);
            final Artist songArtist = getSongArtist(audioFile, musicDao, albumArtist);
            getSong(audioFile, musicDao, album, songArtist);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(AudioFileReader.class.getName(), e.getMessage());
        }
    }

    private Artist getAlbumArtist(AudioFile audioFile, MusicDao musicDao) {
        String albumArtistName = audioFile.getTag().getFirst(FieldKey.ALBUM_ARTIST);
        if (albumArtistName == null || albumArtistName.trim().isEmpty()) {
            albumArtistName = audioFile.getTag().getFirst(FieldKey.ARTIST);
        }
        if (albumArtistName == null || albumArtistName.trim().isEmpty()) {
            throw new RuntimeException("Albums's artist is mandatory! File: '" + audioFile.getFile() + "'");
        }

        return musicDao.findOrCreateArtist(albumArtistName, this::handleArtistInsertion);
    }

    private Album getAlbum(AudioFile audioFile, MusicDao musicDao, Artist albumArtist) {
        final String albumName = audioFile.getTag().getFirst(FieldKey.ALBUM);
        String yearString = audioFile.getTag().getFirst(FieldKey.YEAR);

        Integer albumYear;
        if (yearString != null && !yearString.isEmpty()) {
            if (yearString.matches("\\d{4}-\\d{2}-\\d{2}") || yearString.matches("\\d{4}-\\d{2}")) {
                yearString = yearString.substring(0, 4);
            }
            albumYear = Integer.parseInt(yearString);
            if (albumYear < 100) {
                if (albumYear <= Calendar.getInstance().get(Calendar.YEAR) - 2000) {
                    albumYear += 2000;
                } else {
                    albumYear += 1900;
                }
            }
        } else {
            albumYear = null;
        }

        return musicDao.findOrCreateAlbum(albumName, albumArtist.id, albumYear, this::handleAlbumInsertion);
    }

    private Artist getSongArtist(AudioFile audioFile, MusicDao musicDao, Artist albumArtist) {
        String songArtistName = audioFile.getTag().getFirst(FieldKey.ARTIST);
        if (songArtistName == null || songArtistName.trim().isEmpty()) {
            return albumArtist;
        } else {
            return musicDao.findOrCreateArtist(songArtistName, this::handleArtistInsertion);
        }
    }

    private void getSong(AudioFile audioFile, MusicDao musicDao, Album album, Artist songArtist) throws IOException {
        final String songName = audioFile.getTag().getFirst(FieldKey.TITLE);
        final Integer songLength;
        if (audioFile.getFile().toString().endsWith(".mp3")) {
            songLength = null;
        } else {
            songLength = audioFile.getAudioHeader().getTrackLength();
        }
        final String songTrack = audioFile.getTag().getFirst(FieldKey.TRACK);

        // TODO: '-1' will actually mess things up here. To be fixed.
        // TODO: possible solution: downstream '-1' could be replaced by the amount of existing songs in the album + 1
        // TODO: possible (try-this-first) solution: retrieve digits from the song filename.
        // The regex is needed because sometimes the track is stored as "4/10" or something.
        final int track = songTrack != null && !songTrack.isEmpty() ? Integer.parseInt(songTrack.split("[^0-9]+")[0]) : -1;

        // TODO: for clarification: rename 'findOrCreateSong' to 'createSong'??? Can it be that the song already exists???
        musicDao.findOrCreateSong(songName, songArtist.id, album.id, track,
                audioFile.getFile().getCanonicalPath(), songLength, this::handleSongInsertion);
    }

    private void handleArtistInsertion(Artist artist) {
        MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this.context);
        builder
                .setType(MLBT_ARTIST_INSERTED)
                .setRecordId(MLBPT_ARTIST_ID, artist.id)
                .buildAndSubmitBroadcast();
        builder
                .setType(MLBT_ARTIST_INSERTED)
                .setClass(ArtistInfoFetcherService.class)
                .setRecordId(MLBPT_ARTIST_ID, artist.id)
                .buildAndSubmitService();
    }

    private void handleAlbumInsertion(Album album) {
        MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this.context);
        builder
                .setType(MLBT_ALBUM_INSERTED)
                .setRecordId(MLBPT_ALBUM_ID, album.id)
                .buildAndSubmitBroadcast();
        builder
                .setType(MLBT_ALBUM_INSERTED)
                .setClass(AlbumInfoFetcherService.class)
                .setRecordId(MLBPT_ALBUM_ID, album.id)
                .buildAndSubmitService();
    }

    private void handleSongInsertion(Song song) {
        MusicLibraryBroadcastBuilder builder = new MusicLibraryBroadcastBuilder(this.context);
        builder
                .setType(MLBT_SONG_INSERTED)
                .setRecordId(MLBPT_SONG_ID, song.id)
                .buildAndSubmitBroadcast();

        if (song.file.endsWith(".mp3")) {
            Intent intent = new Intent(context, Mp3ScanningService.class);
            intent.putExtra(MP3PT_SONG_ID.name(), song.id);
            context.startService(intent);
        }
    }
}
