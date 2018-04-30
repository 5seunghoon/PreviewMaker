package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PreviewItem {
  private final static String TAG = "PreviewEditActivity";
  private static int bitmapMaxSize = 1000;
  private Uri originalImageURI;
  private Uri thumbnailImageURI;
  private Bitmap mBitmap;
  private Activity mActivity;

  public static Bitmap URIToBitmap(Uri imageUri, Activity activity){
    Bitmap bitmap = null;
    Bitmap resizedBitmap = null;
    int width = 1, height = 1;
    double rate;


    try {
      bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
      width = bitmap.getWidth();
      height = bitmap.getHeight();
      rate = width/height;
      if(rate > 1) { // w > h
        resizedBitmap = bitmap.createScaledBitmap(bitmap, bitmapMaxSize, (int) (bitmapMaxSize * (1 / rate)), true);
        Logger.d(TAG, "W : " + bitmapMaxSize + " , H : " +  (int) (bitmapMaxSize * (1 / rate)));
      } else { // h > w
        resizedBitmap = bitmap.createScaledBitmap(bitmap, (int) (bitmapMaxSize * (rate)), bitmapMaxSize, true);
        Logger.d(TAG, "W : " + (int) (bitmapMaxSize * (rate)) + " , H : " + bitmapMaxSize);
      }
      Logger.d(TAG, "URI -> Bitmap success : URI : " + imageUri);
    } catch (FileNotFoundException e) {
      Logger.d(TAG, "URI -> Bitmap : URI File not found" + imageUri);
      e.printStackTrace();
    } catch (IOException e) {
      Logger.d(TAG, "URI -> Bitmap : IOException" + imageUri);
      e.printStackTrace();
    }
    return resizedBitmap;
  }

  public PreviewItem(Uri originalImageURI, Uri thumbnailImageURI, Activity activity){
    this.originalImageURI = originalImageURI;
    this.thumbnailImageURI = thumbnailImageURI;
    this.mActivity = activity;
    mBitmap = URIToBitmap(originalImageURI, mActivity);
  }

  public void setOriginalImageURI(Uri originalImageURI) {
    this.originalImageURI = originalImageURI;
    mBitmap = URIToBitmap(originalImageURI, mActivity);
  }

  public void setThumbnailImageURI(Uri thumbnailImageURI) {
    this.thumbnailImageURI = thumbnailImageURI;
  }

  public Uri getOriginalImageURI() {
    return originalImageURI;
  }

  public Uri getThumbnailImageURI() {
    return thumbnailImageURI;
  }

  public Bitmap getmBitmap() {
    return mBitmap;
  }

}
