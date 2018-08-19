package com.wilco375.settingseditor.xposed;

import android.util.Log;

import com.wilco375.settingseditor.BuildConfig;

import de.robv.android.xposed.XposedBridge;

/**
 * Convenience class for logging to {@link XposedBridge#log(String)}
 */
public class Logger {
    /**
     * Log flag, gets prepended to each log message
     */
    private static final String FLAG = "[Settings Editor] ";

    /**
     * Time of previous log. Used to calculate the time delta between two logs so that delays become visible
     */
    private static long lastTime = System.currentTimeMillis();

    /**
     * Set to true to log debug messages
     */
    public static boolean debug = false;

    /**
     * Log message using {@link #log(String)} if the {@link #debug} variable is true or if the build variant is Debug
     *
     * @param message message to log
     */
    public static void logDbg(String message) {
        if (BuildConfig.DEBUG || debug) log(message);
    }

    /**
     * Log message to {@link XposedBridge#log(String)} (or to the Logcat if not in Xposed context)
     *
     * @param message message to log
     */
    public static void log(String message) {
        message = "Delta: " + (System.currentTimeMillis() - lastTime) + "    " + message;
        try {
            XposedBridge.log(FLAG + message);
            lastTime = System.currentTimeMillis();
        } catch (NoClassDefFoundError e) {
            // Not in Xposed Context, log to Logcat
            Log.i(FLAG, message);
        }
    }
}
