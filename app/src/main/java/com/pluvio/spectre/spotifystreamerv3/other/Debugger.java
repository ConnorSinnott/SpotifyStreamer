package com.pluvio.spectre.spotifystreamerv3.other;

import android.util.Log;

/**
 * Created by Spectre on 8/15/2015.
 */
public class Debugger {

    private static final String DEBUGGER_TAG = "[Debugger]";

    public static void print(Object in) {
        print(Log.VERBOSE, "", in);
    }

    public static void print(String tag, Object in) {
        print(Log.VERBOSE, tag, in);
    }

    public static void print(int priority, String tag, Object in) {
        Log.println(priority, DEBUGGER_TAG + " " + tag, in.toString());
    }

}
