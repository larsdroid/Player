package org.willemsens.player.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(indices = {
        @Index(value = {"path"},
                unique = true)
})
public class Directory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String path;

    public Date scanTimestamp;

    public Directory(@NonNull String path) {
        this.path = path;
    }
}
