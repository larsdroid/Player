package org.willemsens.player.view.main.music;

import android.support.annotation.IdRes;
import org.willemsens.player.R;

public enum SubFragmentType {
    ALBUMS(0, R.id.navigation_albums), ARTISTS(1, R.id.navigation_artists), SONGS(2, R.id.navigation_songs);

    private int index;

    @IdRes
    private int menuItemResId;

    SubFragmentType(int index, @IdRes int menuItemResId) {
        this.index = index;
        this.menuItemResId = menuItemResId;
    }

    public int getIndex() {
        return index;
    }

    public static SubFragmentType get(@IdRes int fragmentResId) {
        for (SubFragmentType subFragmentType : values()) {
            if (subFragmentType.menuItemResId == fragmentResId) {
                return subFragmentType;
            }
        }
        return null;
    }

    public static SubFragmentType getByIndex(int index) {
        for (SubFragmentType subFragmentType : values()) {
            if (subFragmentType.index == index) {
                return subFragmentType;
            }
        }
        return null;
    }
}
