package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PreviewCanvasView extends View {
  private final static String TAG = "PreviewEditActivity";
  private Canvas mCanvas;
  private Activity mActivity;

  public static Bitmap URIToBitmap(Uri imageUri, Activity activity){
    Bitmap bitmap = null;
    try {
      bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
    } catch (FileNotFoundException e) {
      Logger.d(TAG, "URI -> Bitmap : URI File not found" + imageUri);
      e.printStackTrace();
    } catch (IOException e) {
      Logger.d(TAG, "URI -> Bitmap : IOException" + imageUri);
      e.printStackTrace();
    }
    return bitmap;
  }

  public PreviewCanvasView(Context context, Activity activity) {
    super(context);
    mActivity = activity;
  }
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mCanvas = canvas;
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundGray));
  }

  protected void setBackImage(Uri imageUri){
  }

}
