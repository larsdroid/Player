package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

public class CurrentAlbumOrSongMessage implements Parcelable {
    private final long albumId;

    public CurrentAlbumOrSongMessage(long albumId) {
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

    public static final Parcelable.Creator<CurrentAlbumOrSongMessage> CREATOR
            = new Parcelable.Creator<CurrentAlbumOrSongMessage>() {
        public CurrentAlbumOrSongMessage createFromParcel(Parcel in) {
            return new CurrentAlbumOrSongMessage(in);
        }

        public CurrentAlbumOrSongMessage[] newArray(int size) {
            return new CurrentAlbumOrSongMessage[size];
        }
    };

    private CurrentAlbumOrSongMessage(Parcel in) {
        this.albumId = in.readLong();
    }
}
