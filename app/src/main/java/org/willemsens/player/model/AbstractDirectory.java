package org.willemsens.player.model;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

import java.util.Date;

@Entity
public abstract class AbstractDirectory {
    /*
    FIXME
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }*/

    @Key
    @Generated
    long id;

    String path;
    Date scanTimestamp;
}
