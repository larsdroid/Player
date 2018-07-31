package org.willemsens.player;

import android.app.Application;
import android.os.StrictMode;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class PlayerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }
        StrictMode.enableDefaults();
    }
}
