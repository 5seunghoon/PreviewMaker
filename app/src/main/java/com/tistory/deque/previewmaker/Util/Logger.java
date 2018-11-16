package com.tistory.deque.previewmaker.Util;

import android.util.Log;


public class Logger {
    private static final String LOGGER_TAG = " [LOGGER TAG]";
    private static boolean printlog = true;

    public static final void w(String TAG, String message) {
        if (printlog) {
            Log.w(TAG + LOGGER_TAG, message);
        }
    }

    public static final void i(String TAG, String message) {
        if (printlog) {
            Log.i(TAG + LOGGER_TAG, message);
        }
    }

    public static final void d(String TAG, String message) {
        if (printlog) {
            Log.d(TAG + LOGGER_TAG, message);
        }
    }

    public static final void v(String TAG, String message) {
        if (printlog) {
            Log.v(TAG + LOGGER_TAG, message);
        }
    }
}
