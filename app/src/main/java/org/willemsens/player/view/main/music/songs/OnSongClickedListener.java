package org.willemsens.player.view.main.music.songs;

import org.willemsens.player.persistence.entities.Song;

public interface OnSongClickedListener {
    void songClicked(Song song);
}
