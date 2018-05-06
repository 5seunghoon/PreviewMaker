package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PreviewCanvasView extends View {
  private final static String TAG = "PreviewEditActivity";


  private static String STATE_NONE_CLICK = "NONE_CLICK_STATE";
  private static String STATE_STAMP_CLICK = "STAMP_CLICK_STATE";
  private static String STATE_STAMP_CLICK_EDIT = "STAMP_EDIT_STATE";
  private static String STATE_PREVIEW_CLICK = "PREVIEW_CLICK_STATE";
  private static String STATE_PREVIEW_CLICK_EDIT = "PREVIEW_EDIT_STATE";
  private static ClickState CLICK_STATE;

  private Canvas mCanvas;
  private PreviewEditActivity mActivity;
  private int canvasWidth, canvasHeight;
  public static int grandParentWidth, grandParentHeight;

  ArrayList<PreviewItem> previewItems;
  private int previewPosWidth, previewPosHeight;
  private int previewPosWidthDelta, previewPosHeightDelta;
  private int previewWidth, previewHeight;
  private double previewZoomRate = 1;

  private boolean isStampShown = false;
  private StampItem stampItem;
  private int stampWidth, stampHeight, stampPosWidthPer, stampPosHeightPer;
  private Uri stampURI;
  private Bitmap stampOriginalBitmap;
  private int stampWidthPos, stampHeightPos;

  private int movePrevX, movePrevY;
  private boolean canMoveStamp = false;


  public PreviewCanvasView(Context context, PreviewEditActivity activity, ArrayList<PreviewItem> previewItems) {
    super(context);
    mActivity = activity;
    this.previewItems = previewItems;
    CLICK_STATE = ClickState.getClickState();
    CLICK_STATE.start();
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
        if(CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_STAMP_CLICK_EDIT){
          drawStampEditRect();
        }
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
    /**
     * 멀티터치를 통한 확대/회전 구현해야함
     * 프리뷰는 확대만, 스탬프는 확대 회전 둘다
     * 스탬프의경우 삭제도 만들어야됨
     */
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
      CLICK_STATE.clickStamp();
    } else if(isTouchInPreview(x,y)) {
      movePrevX = x;
      movePrevY = y;
      CLICK_STATE.clickPreview();
    }
    mActivity.editButtonGoneOrVisible(CLICK_STATE);
  }


  private void touchMove(MotionEvent event){
    int x, y;
    x = (int) event.getX();
    y = (int) event.getY();
    int deltaX = x - movePrevX;
    int deltaY = y - movePrevY;

    switch (CLICK_STATE.getClickStateEnum()){
      case STATE_STAMP_CLICK_EDIT:

        stampWidthPos += deltaX;
        stampHeightPos += deltaY;
        movePrevX = x;
        movePrevY = y;
        invalidate();
        break;
      /**
       *
       case STATE_PREVIEW_CLICK:

       previewPosWidthDelta += deltaX;
       previewPosHeightDelta += deltaY;
       movePrevX = x;
       movePrevY = y;
       invalidate();
       break;
       */
    }

  }
  private void touchUp(MotionEvent event){
    //CLICK_STATE = ClickState.STATE_NONE_CLICK;
  }

  private boolean isTouchInStamp(int x, int y){
    if(x < stampWidthPos + stampWidth && x > stampWidthPos){
      if(y < stampHeightPos + stampHeight && y > stampHeightPos){
        return true;
      }
    }
    return false;
  }

  private boolean isTouchInPreview(int x, int y) {
    if(x < previewPosWidth + previewWidth && x > previewPosWidth){
      if( y < previewPosHeight + previewHeight && y > previewPosHeight){
        return true;
      }
    }
    return false;
  }

  private void drawBellowBitmap() {
    Bitmap previewBitmap = previewItems.get(PreviewEditActivity.POSITION).getmBitmap();
    //canvasWidth = mCanvas.getWidth();
    //canvasHeight = mCanvas.getHeight();
    canvasWidth = grandParentWidth - 16;
    canvasHeight = grandParentHeight - 16; // layout margin
    int previewBitmapWidth = previewBitmap.getWidth();
    int previewBitmapHeight = previewBitmap.getHeight();
    Logger.d(TAG, "CANVAS : W : " + canvasWidth + " , H : " + canvasHeight);

    double rate = (double) previewBitmapWidth / (double) previewBitmapHeight;
    Rect dst;

    if(rate > 1 && previewBitmapWidth > canvasWidth) { // w > h

      previewPosWidth = 0;
      previewPosHeight = (canvasHeight - (int) (canvasWidth * (1 / rate))) / 2;
      previewWidth = canvasWidth;
      previewHeight = (int) (canvasWidth * (1 / rate));

    } else if (rate <= 1 && previewBitmapHeight > canvasHeight) { // w < h

      previewPosWidth = (canvasWidth -(int) (canvasHeight * (rate))) / 2;
      previewPosHeight = 0;
      previewWidth = (int) (canvasHeight * (rate));
      previewHeight = canvasHeight;

    } else {

      previewPosWidth = (canvasWidth - previewBitmapWidth) / 2;
      previewPosHeight = (canvasHeight - previewBitmapHeight) / 2;
      previewWidth = previewBitmapWidth;
      previewHeight = previewBitmapHeight;

    }

    previewPosWidth += previewPosWidthDelta;
    previewPosHeight += previewPosHeightDelta;

    //mCanvas.drawBitmap(previewBitmap, previewPosWidth, previewPosHeight, null); // put center
    dst = new Rect(previewPosWidth, previewPosHeight, previewPosWidth + previewWidth, previewPosHeight + previewHeight);
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

  private void drawStampEditRect() {

  }

  public void finishStampEdit(){
    CLICK_STATE.clickFinishStampEdit();
    mActivity.editButtonGoneOrVisible(CLICK_STATE);
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

  public void savePreviewAll(){
    //SaveAllAsyncTask saveAllAsyncTask = new SaveAllAsyncTask();
    //saveAllAsyncTask.execute(previewItems.size());
  }

  public void savePreviewEach(int previewPosition, PreviewCanvasView v){
    /**
     * 저장시 할 일
     * 1. 이미지를 원래 크기로 다시 확대(or 축소)
     * 2. 그 확대된 비율과, 낙관이 원래 붙어있던 위치에 맞춰서 확대된 이미지에도 제대로 낙관 붙이고
     * 3. 크기 맞춰서 자르고 (3000*3000을 프리뷰 원래 크기에 맞춰서 자른다는 뜻)
     * 4. 그 상태로 이미지로 저장
     * 5. 다시 축소해서 되돌리기
     */
    PreviewEditActivity.POSITION = previewPosition;
    v.callInvalidate();

    Bitmap screenshot = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(screenshot);
    v.draw(canvas);

    Uri resultUri = previewItems.get(previewPosition).getResultImageURI();
    String resultFilePath = resultUri.getPath();
    File resultFile = new File(resultFilePath);
    FileOutputStream fos;
    try{
      fos = new FileOutputStream(resultFile);
      screenshot.compress(Bitmap.CompressFormat.JPEG, 100, fos);
      fos.close();
      Snackbar.make(this, "저장 성공 : " + resultFilePath, Snackbar.LENGTH_LONG).show();

      Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      mediaScanIntent.setData(resultUri);
      mActivity.sendBroadcast(mediaScanIntent);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Snackbar.make(this, "저장 실패...", Snackbar.LENGTH_LONG).show();
    } catch (IOException e) {
      e.printStackTrace();
      Snackbar.make(this, "저장 실패...", Snackbar.LENGTH_LONG).show();
    }

    CLICK_STATE.clickSave();

    PreviewItem previewItem = previewItems.get(PreviewEditActivity.POSITION);
    previewItem.setOriginalImageURI(resultUri);
    previewItem.saved();
  }

  public void clickNewPreview(final int nextPosition) {
    AlertDialog.Builder stampDeleteAlert = new AlertDialog.Builder(mActivity);
    stampDeleteAlert.setMessage("편집 중인 프리뷰를 저장하시겠어요?").setCancelable(true)
      .setPositiveButton("YES", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          savePreviewEach(PreviewEditActivity.POSITION, PreviewCanvasView.this);
          changePreviewInCanvas(nextPosition);
          return;
        }
      })
      .setNegativeButton("NO",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            changePreviewInCanvas(nextPosition);
            return;
          }
        });

    if(PreviewEditActivity.POSITION != -1){
      AlertDialog alert = stampDeleteAlert.create();
      alert.show();
    }
  }

  public void changePreviewInCanvas(int nextPosition){
    previewValueInit();
    PreviewEditActivity.POSITION = nextPosition;
    isStampShown = false;
    invalidate();
  }
  public void previewValueInit(){
    previewPosWidth = 0;
    previewPosHeight = 0;
    previewPosWidthDelta = 0;
    previewPosHeightDelta = 0;
    previewWidth = 0;
    previewHeight = 0;
    previewZoomRate = 1;
  }

  protected class SaveAllAsyncTask extends AsyncTask<Integer, Integer, Integer>{
    @Override
    protected Integer doInBackground(Integer... integers) {
      for(int i = 0 ; i < integers[0] ; i ++){
        savePreviewEach(i, PreviewCanvasView.this);
      }
      return null;
    }
  }
}
