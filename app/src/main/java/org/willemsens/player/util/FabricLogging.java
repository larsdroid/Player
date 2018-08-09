package org.willemsens.player.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class FabricLogging {
    public static void logExceptionToFabric(Exception e) {
        Writer writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        Answers.getInstance().logCustom(new CustomEvent("Exception")
                .putCustomAttribute("Exception Message", e.getMessage())
                .putCustomAttribute("Exception Trace", writer.toString()));

        // Doesn't work:
        // Crashlytics.logException(e);
    }
}
