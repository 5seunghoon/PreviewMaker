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
import java.util.ArrayList;

public class PreviewCanvasView extends View {
  private final static String TAG = "PreviewEditActivity";
  private Canvas mCanvas;
  private Activity mActivity;
  ArrayList<PreviewItem> previewItems;

  public PreviewCanvasView(Context context, Activity activity, ArrayList<PreviewItem> previewItems) {
    super(context);
    mActivity = activity;
    this.previewItems = previewItems;
  }
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mCanvas = canvas;
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundGray));
    if(PreviewEditActivity.POSITION < 0) { // 프리뷰들중에서 아무런 프리뷰도 선택하지 않았을 때
      setBackgroundColor(Color.BLUE);
    } else {
      mCanvas.drawBitmap(previewItems.get(PreviewEditActivity.POSITION).getmBitmap(), 0, 0, null);
    }
  }

  protected void callInvalidate(){
    invalidate();
  }

}
