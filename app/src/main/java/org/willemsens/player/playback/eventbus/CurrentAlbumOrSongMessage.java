package org.willemsens.player.playback.eventbus;

import android.os.Parcel;
import android.os.Parcelable;
import org.willemsens.player.persistence.entities.helpers.SongWithAlbumInfo;

public class CurrentAlbumOrSongMessage implements Parcelable {
    private final SongWithAlbumInfo song;

    public CurrentAlbumOrSongMessage(SongWithAlbumInfo song) {
        this.song = song;
    }

    public SongWithAlbumInfo getSong() {
        return song;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.song, 0);
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
        this.song = in.readParcelable(SongWithAlbumInfo.class.getClassLoader());
    }
}
