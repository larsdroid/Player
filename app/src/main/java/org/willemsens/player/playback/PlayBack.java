package org.willemsens.player.playback;

import org.willemsens.player.model.Song;

public class PlayBack {
    private Song currentSong;
    private PlayMode playMode;
    private PlayStatus playStatus;

    private static PlayBack instance;

    private PlayBack() {
    }

    public static PlayBack getInstance() {
        if (instance == null) {
            instance = new PlayBack();
        }
        return instance;
    }

    void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    void setPlayStatus(PlayStatus playStatus) {
        this.playStatus = playStatus;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public PlayStatus getPlayStatus() {
        return playStatus;
    }
}
