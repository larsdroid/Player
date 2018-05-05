package org.willemsens.player.filescanning;

import android.util.Log;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.willemsens.mp3_vbr_length.Mp3Info;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Song;
import org.willemsens.player.persistence.MusicDao;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class AudioFileReader {
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    static Song readSong(File file, MusicDao musicDao) {
        try {
            final AudioFile audioFile = AudioFileIO.read(file);
            final Artist albumArtist = getAlbumArtist(audioFile, musicDao);
            final Album album = getAlbum(audioFile, musicDao, albumArtist);
            final Artist songArtist = getSongArtist(audioFile, musicDao, albumArtist);
            final Song song = getSong(audioFile, musicDao, album, songArtist);

            Log.v(AudioFileReader.class.getName(), "SONG: " + song);

            return song;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(AudioFileReader.class.getName(), e.getMessage());
            return null;
        }
    }

    private static Artist getAlbumArtist(AudioFile audioFile, MusicDao musicDao) {
        final String albumArtistName = audioFile.getTag().getFirst(FieldKey.ALBUM_ARTIST);
        if (albumArtistName == null || albumArtistName.trim().isEmpty()) {
            throw new RuntimeException("Albums's artist is mandatory! File: '" + audioFile.getFile() + "'");
        }

        return musicDao.findOrCreateArtist(albumArtistName, artist -> {
            // TODO: immediate broadcast
        });
    }

    private static Album getAlbum(AudioFile audioFile, MusicDao musicDao, Artist albumArtist) {
        final String albumName = audioFile.getTag().getFirst(FieldKey.ALBUM);
        String yearString = audioFile.getTag().getFirst(FieldKey.YEAR);
        Integer albumYear = null;
        if (yearString != null && !yearString.isEmpty()) {
            if (yearString.matches("\\d{4}-\\d{2}-\\d{2}")) {
                yearString = yearString.substring(0, 4);
            }
            albumYear = Integer.parseInt(yearString);
        }
        final Album album = new Album(albumName, albumArtist.id);
        album.yearReleased = albumYear;

        // TODO: INSERT album here!!!! --> findOrCreate
        // TODO: immediate broadcast in case of create

        return album;
    }

    private static Artist getSongArtist(AudioFile audioFile, MusicDao musicDao, Artist albumArtist) {
        String songArtistName = audioFile.getTag().getFirst(FieldKey.ARTIST);
        if (songArtistName == null || songArtistName.trim().isEmpty()) {
            return albumArtist;
        } else {
            return musicDao.findOrCreateArtist(songArtistName, artist -> {
                // TODO: immediate broadcast
            });
        }
    }

    private static Song getSong(AudioFile audioFile, MusicDao musicDao, Album album, Artist songArtist) throws IOException {
        final String songName = audioFile.getTag().getFirst(FieldKey.TITLE);
        final int songLength;
        if (audioFile.getFile().toString().endsWith(".mp3")) {
            // This takes a while!!
            final Mp3Info mp3Info = Mp3Info.of(audioFile.getFile());
            songLength = mp3Info.getSeconds();
        } else {
            songLength = audioFile.getAudioHeader().getTrackLength();
        }
        final String songTrack = audioFile.getTag().getFirst(FieldKey.TRACK);
        final int track = songTrack != null && !songTrack.isEmpty() ? Integer.parseInt(songTrack) : -1;
        final Song song = new Song(songName, songArtist.id, album.id, track, audioFile.getFile().getCanonicalPath());
        song.length = songLength;

        // TODO: INSERT song here!!!! --> findOrCreate
        // TODO: immediate broadcast in case of create

        return song;
    }
}
