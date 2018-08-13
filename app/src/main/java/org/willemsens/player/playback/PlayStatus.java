package org.willemsens.player.playback;

import android.os.Parcel;
import android.os.Parcelable;

public enum PlayStatus implements Parcelable {
    STOPPED, PLAYING, PAUSED;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public static final Creator<PlayStatus> CREATOR = new Creator<PlayStatus>() {
        @Override
        public PlayStatus createFromParcel(Parcel in) {
            return PlayStatus.values()[in.readInt()];
        }

        @Override
        public PlayStatus[] newArray(int size) {
            return new PlayStatus[size];
        }
    };
}
