package com.github.dfa.diaspora_android.util;

/**
 * Created by gregor on 18.09.16.
 */
public class AppLog {
    private final static String APP_TAG = "d*";
    private static boolean loggingEnabled = true;
    private static boolean loggingSpamEnabled = false;

    public static boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public static void setLoggingEnabled(boolean loggingEnabled) {
        AppLog.loggingEnabled = loggingEnabled;
    }

    public static boolean isLoggingSpamEnabled() {
        return loggingSpamEnabled;
    }

    public static void setLoggingSpamEnabled(boolean loggingSpamEnabled) {
        AppLog.loggingSpamEnabled = loggingSpamEnabled;
    }

    private static String getLogPrefix(Object source) {
        return APP_TAG + "-" + source.getClass().getCanonicalName();
    }

    /*
     *
     * LOGGER METHODS
     *
     */
    public static void v(Object source, String _text) {
        if (isLoggingEnabled()) {
            Log.v(getLogPrefix(source), _text);
        }
    }

    public static void i(Object source, String _text) {
        if (isLoggingEnabled()) {
            Log.i(getLogPrefix(source), _text);
        }
    }

    public static void d(Object source, String _text) {
        if (isLoggingEnabled()) {
            Log.d(getLogPrefix(source), _text);
        }
    }

    public static void e(Object source, String _text) {
        if (isLoggingEnabled()) {
            Log.e(getLogPrefix(source), _text);
        }
    }

    public static void w(Object source, String _text) {
        if (isLoggingEnabled()) {
            Log.w(getLogPrefix(source), _text);
        }
    }

    public static void spam(Object source, String _text) {
        if (isLoggingEnabled() && isLoggingSpamEnabled()) {
            Log.v(getLogPrefix(source), _text);
        }
    }
}
