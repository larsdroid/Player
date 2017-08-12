package org.willemsens.player.imagefetchers;

import org.willemsens.player.model.InfoSource;

public abstract class FetchedInfo {
    private final InfoSource infoSource;

    protected FetchedInfo(InfoSource infoSource) {
        this.infoSource = infoSource;
    }

    public InfoSource getInfoSource() {
        return infoSource;
    }
}
