package org.willemsens.player;

import android.app.Application;
import android.os.StrictMode;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;

public class PlayerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.USE_CRASHLYTICS) {
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().disabled(false).build())
                    .build();

            Fabric.with(this, crashlyticsKit);
        }

        if (BuildConfig.USE_ANSWERS) {
            Fabric.with(this, new Answers());
        }

        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
    }
}
