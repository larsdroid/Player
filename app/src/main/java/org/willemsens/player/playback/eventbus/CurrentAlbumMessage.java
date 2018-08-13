package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

public class CurrentAlbumMessage implements Parcelable {
    private final long albumId;

    public CurrentAlbumMessage(long albumId) {
        this.albumId = albumId;
    }

    public long getAlbumId() {
        return albumId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.albumId);
    }

    public static final Parcelable.Creator<CurrentAlbumMessage> CREATOR
            = new Parcelable.Creator<CurrentAlbumMessage>() {
        public CurrentAlbumMessage createFromParcel(Parcel in) {
            return new CurrentAlbumMessage(in);
        }

        public CurrentAlbumMessage[] newArray(int size) {
            return new CurrentAlbumMessage[size];
        }
    };

    private CurrentAlbumMessage(Parcel in) {
        this.albumId = in.readLong();
    }
}
