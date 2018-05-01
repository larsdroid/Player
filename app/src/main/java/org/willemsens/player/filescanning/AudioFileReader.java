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
import java.util.logging.Level;
import java.util.logging.Logger;

class AudioFileReader {
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    static Song readSong(File file, MusicDao musicDao) {
        try {
            final AudioFile audioFile = AudioFileIO.read(file);

            final String albumArtistName = audioFile.getTag().getFirst(FieldKey.ALBUM_ARTIST);
            if (albumArtistName == null || albumArtistName.trim().isEmpty()) {
                Log.e(AudioFileReader.class.getName(), "Albums's artist is mandatory! File: '" + file + "'");
                return null;
            }
            final Artist albumArtist = musicDao.findOrCreateArtist(albumArtistName);
            // TODO: perhaps broadcast!

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

            // TODO: INSERT album here!!!!
            // TODO: immediate broadcast!!!!

            String songArtistName = audioFile.getTag().getFirst(FieldKey.ARTIST);
            if (songArtistName == null || songArtistName.trim().isEmpty()) {
                songArtistName = albumArtistName;
            }
            final Artist songArtist = musicDao.findOrCreateArtist(songArtistName);
            // TODO: perhaps broadcast!

            final String songName = audioFile.getTag().getFirst(FieldKey.TITLE);
            final int songLength;
            if (file.toString().endsWith(".mp3")) {
                // This takes a while!!
                final Mp3Info mp3Info = Mp3Info.of(file);
                songLength = mp3Info.getSeconds();
            } else {
                songLength = audioFile.getAudioHeader().getTrackLength();
            }
            final String songTrack = audioFile.getTag().getFirst(FieldKey.TRACK);
            final int track = songTrack != null && !songTrack.isEmpty() ? Integer.parseInt(songTrack) : -1;
            final Song song = new Song(songName, songArtist.id, album.id, track, file.getCanonicalPath());
            song.length = songLength;

            Log.v(AudioFileReader.class.getName(), "SONG: " + song);

            return song;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(AudioFileReader.class.getName(), e.getMessage());
            return null;
        }
    }
}
