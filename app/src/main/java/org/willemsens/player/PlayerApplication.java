package org.willemsens.player;

import android.app.Application;
import android.os.StrictMode;

public class PlayerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }
}
