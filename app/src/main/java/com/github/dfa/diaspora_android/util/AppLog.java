/*
    This file is part of the Diaspora for Android.

    Diaspora for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora for Android.

    If not, see <http://www.gnu.org/licenses/>.
 */
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
