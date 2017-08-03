package org.willemsens.player.view;

import org.willemsens.player.persistence.MusicDao;

public interface DataAccessProvider {
    MusicDao getMusicDao();
}
