package org.willemsens.player;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;
import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraLimiter;
import org.acra.annotation.AcraMailSender;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "lars@willemsens.org")
@AcraLimiter(failedReportLimit = 10, period = 10)
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

        StrictMode.enableDefaults();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }
}
