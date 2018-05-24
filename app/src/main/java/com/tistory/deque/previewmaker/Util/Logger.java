package com.tistory.deque.previewmaker.Util;

import android.arch.core.BuildConfig;
import android.util.Log;


public class Logger {
  private static final String LOGGER_TAG = " [LOGGER TAG]";
  public static final void w(String TAG, String message) {
    if (BuildConfig.DEBUG) {
      Log.w(TAG + LOGGER_TAG, message);
    }
  }

  public static final void i(String TAG, String message) {
    if (BuildConfig.DEBUG){
      Log.i(TAG + LOGGER_TAG, message);
    }
  }

  public static final void d(String TAG, String message) {
    if (BuildConfig.DEBUG){
      Log.d(TAG + LOGGER_TAG, message);
    }
  }

  public static final void v(String TAG, String message) {
    if (BuildConfig.DEBUG){
      Log.v(TAG + LOGGER_TAG, message);
    }
  }
}
