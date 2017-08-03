package org.willemsens.player.model;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Directory {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final long id;
    private final String path;
    private Date scanTimestamp;

    public Directory(long id, String path, String txtStamp) {
        this.id = id;
        this.path = path;
        this.scanTimestamp = null;
        if (txtStamp != null) {
            try {
                this.scanTimestamp = DATE_FORMAT.parse(txtStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPath() {
        return path;
    }

    public Date getScanTimestamp() {
        return scanTimestamp;
    }
}
