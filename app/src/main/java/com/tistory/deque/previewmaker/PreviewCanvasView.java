package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
  private PreviewEditActivity mActivity;
  private int canvasWidth, canvasHeight;

  private int previewPosWidth, previewPosHeight;

  private boolean isStampShown = false;
  private StampItem stampItem;
  private int stampWidth, stampHeight, stampPosWidthPer, stampPosHeightPer;
  private Uri stampURI;
  private Bitmap stampOriginalBitmap;
  private int stampWidthPos, stampHeightPos;


  ArrayList<PreviewItem> previewItems;

  public PreviewCanvasView(Context context, PreviewEditActivity activity, ArrayList<PreviewItem> previewItems) {
    super(context);
    mActivity = activity;
    this.previewItems = previewItems;
  }
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mCanvas = canvas;
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundGray));
    if (PreviewEditActivity.POSITION < 0) { // 프리뷰들중에서 아무런 프리뷰도 선택하지 않았을 때
      setBackgroundColor(Color.WHITE);
    } else {
      drawBellowBitmap();
      if (isStampShown){
        Logger.d(TAG, "stamp shown true");
        drawStamp();
      }
    }
  }

  private void drawBellowBitmap() {
    Bitmap previewBitmap = previewItems.get(PreviewEditActivity.POSITION).getmBitmap();
    canvasWidth = mCanvas.getWidth();
    canvasHeight = mCanvas.getHeight();
    int previewBitmapWidth = previewBitmap.getWidth();
    int previewBitmapHeight = previewBitmap.getHeight();
    Logger.d(TAG, "CANVAS : W : " + canvasWidth + " , H : " + canvasHeight);
    previewPosWidth = (canvasWidth - previewBitmapWidth) / 2;
    previewPosHeight = (canvasHeight - previewBitmapHeight) / 2;
    mCanvas.drawBitmap(previewBitmap, previewPosWidth, previewPosHeight, null); // put center
  }

  private void drawStamp(){
    if(stampItem == null) {
      Logger.d(TAG, "no stamp item");
      return;
    }
    Logger.d(TAG, "STAMP draw : w, h, pw, ph, uri : " +
      stampWidth + ", " + stampHeight + ", " +
      stampPosWidthPer + ", " + stampPosHeightPer + ", " + stampURI);

    Bitmap resizedStampBitmap = Bitmap.createScaledBitmap(stampOriginalBitmap, stampWidth, stampHeight,true);

    stampWidthPos = (stampPosWidthPer * canvasWidth / 100) - (stampWidth / 2);
    stampHeightPos = (stampPosHeightPer * canvasHeight / 100) - (stampHeight / 2);

    mCanvas.drawBitmap(resizedStampBitmap, stampWidthPos, stampHeightPos, null);

  }

  protected void callInvalidate(){
    invalidate();
  }

  public boolean isStampShown() {
    return isStampShown;
  }

  public void setStampShown(boolean stampShown) {
    isStampShown = stampShown;
  }

  public StampItem getStampItem() {
    return stampItem;
  }

  public void setStampItem(StampItem stampItem) {
    this.stampItem = stampItem;
    stampWidth = stampItem.getWidth();
    stampHeight = stampItem.getHeight();
    stampPosWidthPer = stampItem.getPos_width_per();
    stampPosHeightPer = stampItem.getPos_height_per();
    stampURI = stampItem.getImageURI();
    stampOriginalBitmap = stampURIToBitmap(stampURI, mActivity);

    if(stampWidth < 0 || stampHeight < 0){
      int id = stampItem.getID();
      stampWidth = stampOriginalBitmap.getWidth();
      stampHeight = stampOriginalBitmap.getHeight();
      mActivity.stampWidthHeightUpdate(id, stampWidth, stampHeight);
    }

    Logger.d(TAG, "STAMP set : w, h, pw, ph, uri : " +
      stampWidth + ", " + stampHeight + ", " +
      stampPosWidthPer + ", " + stampPosHeightPer + ", " + stampURI);
  }

  public Bitmap stampURIToBitmap(Uri imageUri, Activity activity){
    try{
      Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
      return bitmap;
    } catch (IOException e) {
      Logger.d(TAG, "URI -> Bitmap : IOException" + imageUri);
      e.printStackTrace();
      return null;
    }
  }
}
