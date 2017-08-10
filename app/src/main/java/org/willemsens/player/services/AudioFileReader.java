package org.willemsens.player.services;

import android.util.Log;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.willemsens.player.exceptions.PlayerException;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Song;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

class AudioFileReader {
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    static Song readSong(File file) {
        try {
            final AudioFile audioFile = AudioFileIO.read(file);

            final String albumArtistName = audioFile.getTag().getFirst(FieldKey.ALBUM_ARTIST);
            final Artist albumArtist = new Artist();
            albumArtist.setName(albumArtistName);

            final String albumName = audioFile.getTag().getFirst(FieldKey.ALBUM);
            final String yearString = audioFile.getTag().getFirst(FieldKey.YEAR);
            final Integer albumYear = yearString != null && !yearString.isEmpty() ? Integer.parseInt(yearString) : null;
            final Album album = new Album();
            album.setName(albumName);
            album.setArtist(albumArtist);
            album.setYearReleased(albumYear);
            album.setLength(0);

            final String songArtistName = audioFile.getTag().getFirst(FieldKey.ARTIST);
            final Artist songArtist = new Artist();
            songArtist.setName(songArtistName);

            final String songName = audioFile.getTag().getFirst(FieldKey.TITLE);
            final int songLength = audioFile.getAudioHeader().getTrackLength();
            final Song song = new Song();
            song.setName(songName);
            song.setArtist(songArtist);
            song.setAlbum(album);
            song.setLength(songLength);
            song.setFile(file.getCanonicalPath());

            if ((albumArtistName == null || albumArtistName.trim().isEmpty()) && songArtistName != null && !songArtistName.trim().isEmpty()) {
                song.getAlbum().setArtist(songArtist);
            }

            if ((songArtistName == null || songArtistName.trim().isEmpty()) && albumArtistName != null && !albumArtistName.trim().isEmpty()) {
                song.setArtist(albumArtist);
            }

            Log.d(AudioFileReader.class.getName(), "SONG: " + song);

            return song;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(AudioFileReader.class.getName(), e.getMessage());
            throw new PlayerException(e.getMessage());
        }
    }
}
