package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumUpdatedMessage implements Parcelable {
    private final long albumId;

    public AlbumUpdatedMessage(long albumId) {
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

    public static final Parcelable.Creator<AlbumUpdatedMessage> CREATOR
            = new Parcelable.Creator<AlbumUpdatedMessage>() {
        public AlbumUpdatedMessage createFromParcel(Parcel in) {
            return new AlbumUpdatedMessage(in);
        }

        public AlbumUpdatedMessage[] newArray(int size) {
            return new AlbumUpdatedMessage[size];
        }
    };

    private AlbumUpdatedMessage(Parcel in) {
        this.albumId = in.readLong();
    }
}
