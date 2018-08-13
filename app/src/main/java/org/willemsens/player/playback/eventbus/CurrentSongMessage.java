package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

public class CurrentSongMessage implements Parcelable {
    private final long songId;

    public CurrentSongMessage(long songId) {
        this.songId = songId;
    }

    public long getSongId() {
        return songId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.songId);
    }

    public static final Parcelable.Creator<CurrentSongMessage> CREATOR
            = new Parcelable.Creator<CurrentSongMessage>() {
        public CurrentSongMessage createFromParcel(Parcel in) {
            return new CurrentSongMessage(in);
        }

        public CurrentSongMessage[] newArray(int size) {
            return new CurrentSongMessage[size];
        }
    };

    private CurrentSongMessage(Parcel in) {
        this.songId = in.readLong();
    }
}
