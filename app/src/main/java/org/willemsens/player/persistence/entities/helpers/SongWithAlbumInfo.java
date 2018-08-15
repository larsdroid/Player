package org.willemsens.player.persistence.entities.helpers;

import android.os.Parcel;
import android.os.Parcelable;

public class SongWithAlbumInfo implements Parcelable {
    public final long id;
    public final String name;
    public final int track;
    public final Integer length;
    public final long albumId;
    public final String albumName;
    public final byte[] albumImageData;
    public final long artistId;
    public final String artistName;

    public SongWithAlbumInfo(long id,
                             String name,
                             int track,
                             Integer length,
                             long albumId,
                             String albumName,
                             byte[] albumImageData,
                             long artistId,
                             String artistName) {
        this.id = id;
        this.name = name;
        this.track = track;
        this.length = length;
        this.albumId = albumId;
        this.albumName = albumName;
        this.albumImageData = albumImageData;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.track);
        dest.writeSerializable(this.length);
        dest.writeLong(this.albumId);
        dest.writeString(this.albumName);
        dest.writeByteArray(this.albumImageData);
        dest.writeLong(this.artistId);
        dest.writeString(this.artistName);
    }

    public static final Parcelable.Creator<SongWithAlbumInfo> CREATOR
            = new Parcelable.Creator<SongWithAlbumInfo>() {
        public SongWithAlbumInfo createFromParcel(Parcel in) {
            return new SongWithAlbumInfo(in);
        }

        public SongWithAlbumInfo[] newArray(int size) {
            return new SongWithAlbumInfo[size];
        }
    };

    private SongWithAlbumInfo(Parcel in) {
        this(in.readLong(),
                in.readString(),
                in.readInt(),
                (Integer) in.readSerializable(),
                in.readLong(),
                in.readString(),
                in.createByteArray(),
                in.readLong(),
                in.readString());
    }
}
