package org.willemsens.player.playback;

public enum PlayerCommand {
    STOP(0), PLAY(1), PAUSE(2), NEXT(3), PREVIOUS(4), STOP_PLAY_PAUSE(5);

    private final int requestCode;

    PlayerCommand(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }
}
