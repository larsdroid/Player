package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;
import org.willemsens.player.playback.PlayStatus;

public class CurrentPlayStatusMessage implements Parcelable {
    private final PlayStatus playStatus;

    public CurrentPlayStatusMessage(PlayStatus playStatus) {
        this.playStatus = playStatus;
    }

    public PlayStatus getPlayStatus() {
        return playStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.playStatus.ordinal());
    }

    public static final Parcelable.Creator<CurrentPlayStatusMessage> CREATOR
            = new Parcelable.Creator<CurrentPlayStatusMessage>() {
        public CurrentPlayStatusMessage createFromParcel(Parcel in) {
            return new CurrentPlayStatusMessage(in);
        }

        public CurrentPlayStatusMessage[] newArray(int size) {
            return new CurrentPlayStatusMessage[size];
        }
    };

    private CurrentPlayStatusMessage(Parcel in) {
        this.playStatus = PlayStatus.values()[in.readInt()];
    }
}
