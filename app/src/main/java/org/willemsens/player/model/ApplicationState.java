package org.willemsens.player.model;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(indices = {
        @Index(value = {"property"},
                unique = true)
})
public class ApplicationState {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String property;

    @NonNull
    public String value;

    public ApplicationState(@NonNull String property, @NonNull String value) {
        this.property = property;
        this.value = value;
    }
}
