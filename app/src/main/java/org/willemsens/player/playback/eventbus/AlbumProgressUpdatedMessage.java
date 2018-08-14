package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumProgressUpdatedMessage implements Parcelable {
    private final long albumId;
    private final Integer currentTrack;
    private final Integer currentMillisInTrack;
    private final int playCount;

    public AlbumProgressUpdatedMessage(long albumId, Integer currentTrack, Integer currentMillisInTrack, int playCount) {
        this.albumId = albumId;
        this.currentTrack = currentTrack;
        this.currentMillisInTrack = currentMillisInTrack;
        this.playCount = playCount;
    }

    public long getAlbumId() {
        return albumId;
    }

    public Integer getCurrentTrack() {
        return currentTrack;
    }

    public Integer getCurrentMillisInTrack() {
        return currentMillisInTrack;
    }

    public int getPlayCount() {
        return playCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.albumId);
        dest.writeSerializable(this.currentTrack);
        dest.writeSerializable(this.currentMillisInTrack);
        dest.writeInt(this.playCount);
    }

    public static final Parcelable.Creator<AlbumProgressUpdatedMessage> CREATOR
            = new Parcelable.Creator<AlbumProgressUpdatedMessage>() {
        public AlbumProgressUpdatedMessage createFromParcel(Parcel in) {
            return new AlbumProgressUpdatedMessage(in);
        }

        public AlbumProgressUpdatedMessage[] newArray(int size) {
            return new AlbumProgressUpdatedMessage[size];
        }
    };

    private AlbumProgressUpdatedMessage(Parcel in) {
        this.albumId = in.readLong();
        this.currentTrack = (Integer) in.readSerializable();
        this.currentMillisInTrack = (Integer) in.readSerializable();
        this.playCount = in.readInt();
    }
}
