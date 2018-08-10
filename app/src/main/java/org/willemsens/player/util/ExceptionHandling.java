package org.willemsens.player.util;

public class ExceptionHandling {
    public static void submitException(Exception e) {
        /*Writer writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        String exceptionString = writer.toString();

        Answers.getInstance().logCustom(new CustomEvent("Exception")
                .putCustomAttribute("Exception Message", e.getMessage())
                .putCustomAttribute("Exception Trace", exceptionString));*/

        // Doesn't work:
        // Crashlytics.logException(e);

        // OR handleSilentException
        //ACRA.getErrorReporter().handleException(e);

        throw new RuntimeException(e);
    }
}
