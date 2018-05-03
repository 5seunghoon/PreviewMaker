package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PreviewCanvasView extends View {
  private final static String TAG = "PreviewEditActivity";
  private Canvas mCanvas;
  private PreviewEditActivity mActivity;
  private int canvasWidth, canvasHeight;
  public static int grandParentWidth, grandParentHeight;

  private int previewPosWidth, previewPosHeight;

  private boolean isStampShown = false;
  private StampItem stampItem;
  private int stampWidth, stampHeight, stampPosWidthPer, stampPosHeightPer;
  private Uri stampURI;
  private Bitmap stampOriginalBitmap;
  private int stampWidthPos, stampHeightPos;

  private int movePrevX, movePrevY;
  private boolean canMoveStamp = false;

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

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    this.setMeasuredDimension(3000, 3000);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()){
      case MotionEvent.ACTION_DOWN :
        touchDown(event);
        break;
      case MotionEvent.ACTION_MOVE :
        touchMove(event);
        break;
      case MotionEvent.ACTION_UP :
        touchUp(event);
        break;
    }
    return true;
  }

  private void touchDown(MotionEvent event){
    int x, y;
    x = (int) event.getX();
    y = (int) event.getY();

    if(isTouchInStamp(x,y)){
      movePrevX = x;
      movePrevY = y;
      canMoveStamp = true;
    }
  }
  private void touchMove(MotionEvent event){
    if(canMoveStamp){
      int x, y;
      x = (int) event.getX();
      y = (int) event.getY();
      int deltaX = x - movePrevX;
      int deltaY = y - movePrevY;
      stampWidthPos += deltaX;
      stampHeightPos += deltaY;
      movePrevX = x;
      movePrevY = y;
      invalidate();
    }
  }
  private void touchUp(MotionEvent event){
    canMoveStamp = false;
  }

  private boolean isTouchInStamp(int x, int y){
    if(x < stampWidthPos + stampWidth && x > stampWidthPos){
      if(y < stampHeightPos + stampHeight && y > stampHeightPos){
        return true;
      }
    }
    return false;
  }

  private void drawBellowBitmap() {
    Bitmap previewBitmap = previewItems.get(PreviewEditActivity.POSITION).getmBitmap();
    //canvasWidth = mCanvas.getWidth();
    //canvasHeight = mCanvas.getHeight();
    canvasWidth = grandParentWidth;
    canvasHeight = grandParentHeight;
    int previewBitmapWidth = previewBitmap.getWidth();
    int previewBitmapHeight = previewBitmap.getHeight();
    Logger.d(TAG, "CANVAS : W : " + canvasWidth + " , H : " + canvasHeight);

    int canvasMinSize = (canvasWidth < canvasHeight) ? canvasWidth : canvasHeight;
    double rate = (double) previewBitmapWidth / (double) previewBitmapHeight;
    Rect dst;

    if(rate > 1 && previewBitmapWidth > previewBitmapHeight) { // w > h
      previewPosWidth = (canvasWidth - canvasMinSize) / 2;
      previewPosHeight = (canvasHeight - (int) (canvasMinSize * (1 / rate))) / 2;
      dst = new Rect(previewPosWidth, previewPosHeight, previewPosWidth + canvasMinSize, previewPosHeight + (int) (canvasMinSize * (1 / rate)));
    } else if (rate <= 1 && previewBitmapWidth < previewBitmapHeight) { // w < h
      previewPosWidth = (canvasWidth -(int) (canvasMinSize * (rate))) / 2;
      previewPosHeight = (canvasHeight - canvasMinSize) / 2;
      dst = new Rect(previewPosWidth, previewPosHeight, previewPosWidth + (int) (canvasMinSize * (rate)), previewPosHeight + canvasMinSize);
    } else {
      previewPosWidth = (canvasWidth - previewBitmapWidth) / 2;
      previewPosHeight = (canvasHeight - previewBitmapHeight) / 2;
      dst = new Rect(previewPosWidth, previewPosHeight, previewPosWidth + previewBitmapWidth, previewPosHeight + previewBitmapHeight);
    }
    //mCanvas.drawBitmap(previewBitmap, previewPosWidth, previewPosHeight, null); // put center
    mCanvas.drawBitmap(previewBitmap, null, dst, null);
  }

  private void drawStamp(){
    if(stampItem == null) {
      Logger.d(TAG, "no stamp item");
      return;
    }
    Logger.d(TAG, "STAMP draw : w, h, pw, ph, uri : " +
      stampWidth + ", " + stampHeight + ", " +
      stampPosWidthPer + ", " + stampPosHeightPer + ", " + stampURI);

    //Bitmap resizedStampBitmap = Bitmap.createScaledBitmap(stampOriginalBitmap, stampWidth, stampHeight,true);
    //mCanvas.drawBitmap(resizedStampBitmap, stampWidthPos, stampHeightPos, null);
    Rect dst =  new Rect(stampWidthPos, stampHeightPos, stampWidthPos + stampWidth, stampHeightPos + stampHeight);
    mCanvas.drawBitmap(stampOriginalBitmap, null, dst,null);
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
    stampPosWidthPer = stampItem.getPos_width_per();
    stampPosHeightPer = stampItem.getPos_height_per();
    stampURI = stampItem.getImageURI();
    stampOriginalBitmap = stampURIToBitmap(stampURI, mActivity);

    stampWidth = stampItem.getWidth();
    stampHeight = stampItem.getHeight();

    if(stampWidth < 0 || stampHeight < 0){
      int id = stampItem.getID();
      stampWidth = stampOriginalBitmap.getWidth();
      stampHeight = stampOriginalBitmap.getHeight();
      mActivity.stampWidthHeightUpdate(id, stampWidth, stampHeight);
    }

    stampWidthPos = (stampPosWidthPer * canvasWidth / 100) - (stampWidth / 2);
    stampHeightPos = (stampPosHeightPer * canvasHeight / 100) - (stampHeight / 2);

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

  public void savePreview(){
    Bitmap screenshot = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(screenshot);
    draw(canvas);

    String resultFilePath = previewItems.get(PreviewEditActivity.POSITION).getResultImageURI().getPath();
    File resultFile = new File(resultFilePath);
    FileOutputStream fos;
    try{
      fos = new FileOutputStream(resultFile);
      screenshot.compress(Bitmap.CompressFormat.JPEG, 100, fos);
      fos.close();
      Snackbar.make(this, "저장 성공 : " + resultFilePath, Snackbar.LENGTH_LONG).show();

      Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      mediaScanIntent.setData(previewItems.get(PreviewEditActivity.POSITION).getResultImageURI());
      mActivity.sendBroadcast(mediaScanIntent);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Snackbar.make(this, "저장 실패...", Snackbar.LENGTH_LONG).show();
    } catch (IOException e) {
      e.printStackTrace();
      Snackbar.make(this, "저장 실패...", Snackbar.LENGTH_LONG).show();
    }
  }
}
