package com.tistory.deque.previewmaker;

import android.util.Log;

public class Logger {
  public static final void w(String TAG, String message) {
  if (BuildConfig.DEBUG){
    Log.w(TAG, message);
  }
}

  public static final void i(String TAG, String message) {
    if (BuildConfig.DEBUG){
      Log.i(TAG, message);
    }
  }

  public static final void d(String TAG, String message) {
    if (BuildConfig.DEBUG){
      Log.d(TAG, message);
    }
  }

  public static final void v(String TAG, String message) {
    if (BuildConfig.DEBUG){
      Log.v(TAG, message);
    }
  }
}
