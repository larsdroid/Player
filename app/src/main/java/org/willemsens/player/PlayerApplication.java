package org.willemsens.player;

import android.app.Application;
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

        // It looks like Fabric / Crashlytics sockets aren't being tagged as they should so strict mode can't be enabled.
        //   https://github.com/square/okhttp/issues/3537
        // StrictMode.enableDefaults();
    }
}
