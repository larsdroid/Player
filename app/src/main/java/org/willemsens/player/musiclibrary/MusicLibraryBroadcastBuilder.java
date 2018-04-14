package org.willemsens.player.musiclibrary;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import org.willemsens.player.model.Album;
import org.willemsens.player.model.Artist;
import org.willemsens.player.model.Song;

import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ALBUM_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_ARTIST_ID;
import static org.willemsens.player.musiclibrary.MusicLibraryBroadcastPayloadType.MLBPT_SONG_ID;

public class MusicLibraryBroadcastBuilder {
    private final Context context;

    private Intent intent;
    private MusicLibraryBroadcastType type;
    private MusicLibraryBroadcastPayloadType payloadType;
    private boolean serviceClassSet;

    public MusicLibraryBroadcastBuilder(@NonNull Context context) {
        this.context = context;
        resetInternalState();
    }

    private void resetInternalState() {
        this.intent = new Intent();
        this.type = null;
        this.payloadType = null;
        this.serviceClassSet = false;
    }

    public MusicLibraryBroadcastBuilder setType(@NonNull MusicLibraryBroadcastType type) {
        if (this.type == null) {
            this.intent.setAction(type.name());
            this.type = type;
            return this;
        } else {
            throw new RuntimeException("Type was already set!");
        }
    }

    public MusicLibraryBroadcastBuilder setSong(@NonNull Song song) {
        return setRecordId(MLBPT_SONG_ID, song.getId());
    }

    public MusicLibraryBroadcastBuilder setAlbum(@NonNull Album album) {
        return setRecordId(MLBPT_ALBUM_ID, album.getId());
    }

    public MusicLibraryBroadcastBuilder setArtist(@NonNull Artist artist) {
        return setRecordId(MLBPT_ARTIST_ID, artist.getId());
    }

    public MusicLibraryBroadcastBuilder setRecordId(@NonNull MusicLibraryBroadcastPayloadType payloadType, long recordId) {
        if (this.payloadType == null) {
            this.intent.putExtra(payloadType.name(), recordId);
            this.payloadType = payloadType;
            return this;
        } else {
            throw new RuntimeException("Payload was already added!");
        }
    }

    public MusicLibraryBroadcastBuilder setClass(@NonNull Class clazz) {
        if (!this.serviceClassSet) {
            this.intent.setClass(this.context, clazz);
            this.serviceClassSet = true;
            return this;
        } else {
            throw new RuntimeException("Service class was already set!");
        }
    }

    private void checkTypeAndPayloadValid() {
        if (this.type == null) {
            throw new RuntimeException("Type was not set!");
        } else {
            boolean isValid = false;
            switch (this.type) {
                case MLBT_ARTIST_INSERTED:
                    isValid = this.payloadType == MLBPT_ARTIST_ID;
                    break;
                case MLBT_ARTISTS_INSERTED:
                    isValid = this.payloadType == null;
                    break;
                case MLBT_ARTIST_UPDATED:
                    isValid = this.payloadType == MLBPT_ARTIST_ID;
                    break;
                case MLBT_ALBUM_INSERTED:
                    isValid = this.payloadType == MLBPT_ALBUM_ID;
                    break;
                case MLBT_ALBUMS_INSERTED:
                    isValid = this.payloadType == null;
                    break;
                case MLBT_ALBUM_UPDATED:
                    isValid = this.payloadType == MLBPT_ALBUM_ID;
                    break;
                case MLBT_SONG_INSERTED:
                    isValid = this.payloadType == MLBPT_SONG_ID;
                    break;
                case MLBT_SONGS_INSERTED:
                    isValid = this.payloadType == null;
                    break;
            }

            if (!isValid) {
                throw new RuntimeException("Type and payload do not match!");
            }
        }
    }

    private Intent build() {
        final Intent returnIntent = this.intent;
        resetInternalState();
        return returnIntent;
    }

    public void buildAndSubmitBroadcast() {
        checkTypeAndPayloadValid();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this.context);
        lbm.sendBroadcast(build());
    }

    public void buildAndSubmitService() {
        checkTypeAndPayloadValid();

        if (!this.serviceClassSet) {
            throw new RuntimeException("Service class was not set!");
        } else {
            this.context.startService(build());
        }
    }
}
