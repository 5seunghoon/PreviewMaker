package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PreviewItem {
  private final static String TAG = "PreviewEditActivity";
  private static int bitmapMaxSize = 1000;
  private Uri originalImageURI;
  private Uri thumbnailImageURI;
  private Uri resultImageURI;
  private Bitmap mBitmap;
  private Activity mActivity;
  private boolean isSaved;

  public static void setBitmapMaxSize(int size){
    bitmapMaxSize = size;
  }
  public static int getBitmapMaxSize(){ return bitmapMaxSize; }

  public static Bitmap URIToBitmap(Uri imageUri, Activity activity){
    Bitmap bitmap = null;
    Bitmap resizedBitmap = null;
    int width = 1, height = 1;
    double rate;


    try {
      bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
      width = bitmap.getWidth();
      height = bitmap.getHeight();
      rate = (double) width / (double) height;
      if(rate > 1 && width > bitmapMaxSize) { // w > h
        Logger.d(TAG, "RATE : " + rate +" , W : " + bitmapMaxSize + " , H : " +  (int) (bitmapMaxSize * (1 / rate)));
        resizedBitmap = bitmap.createScaledBitmap(bitmap, bitmapMaxSize, (int) (bitmapMaxSize * (1 / rate)), true);
      } else if (rate <= 1 && height > bitmapMaxSize) { // h > w
        Logger.d(TAG, "RATE : " + rate +" , W : " + (int) (bitmapMaxSize * (rate)) + " , H : " + bitmapMaxSize);
        resizedBitmap = bitmap.createScaledBitmap(bitmap, (int) (bitmapMaxSize * (rate)), bitmapMaxSize, true);
      }
      else {
        resizedBitmap = bitmap;
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
    this.isSaved = false;
    this.resultImageURI = makeResultImageFile();
    mBitmap = URIToBitmap(originalImageURI, mActivity);
  }

  public void setOriginalImageURI(Uri originalImageURI) {
    this.originalImageURI = originalImageURI;
    mBitmap = URIToBitmap(originalImageURI, mActivity);
  }

  public void setThumbnailImageURI(Uri thumbnailImageURI) {
    this.thumbnailImageURI = thumbnailImageURI;
  }

  private Uri makeResultImageFile(){
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "PREVIEW_" + timeStamp + ".png";
    File imageFile = null;
    File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", MainActivity.PREVIEW_SAVED_DIRECTORY);
    if(!storageDir.exists()){
      storageDir.mkdir();
    }
    imageFile = new File(storageDir, imageFileName);
    Uri resultUri = Uri.fromFile(imageFile);

    Logger.d(TAG, "storageDir : " + storageDir);
    Logger.d(TAG, "image file name : " + imageFileName);
    Logger.d(TAG, "image file uri : " + resultUri);

    return resultUri;
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

  public boolean getIsSaved() {
    return isSaved;
  }

  public boolean getIsCropped(){
    return isSaved;
  }

  public void cropped(){
    isSaved = true;
  }

  public void saved(){
    isSaved = true;
  }

  public Uri getResultImageURI() {
    return resultImageURI;
  }
}
