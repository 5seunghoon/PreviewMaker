package com.tistory.deque.previewmaker.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import com.tistory.deque.previewmaker.Controler.PreviewBitmapControler;
import com.tistory.deque.previewmaker.Model_Global.ClickState;
import com.tistory.deque.previewmaker.Model_Global.ClickStateEnum;
import com.tistory.deque.previewmaker.Model_PreviewData.PreviewItem;
import com.tistory.deque.previewmaker.R;
import com.tistory.deque.previewmaker.Model_StampData.StampAnchorEnum;
import com.tistory.deque.previewmaker.Model_StampData.StampItem;
import com.tistory.deque.previewmaker.Util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PreviewCanvasView extends View {
    private final static String TAG = "PreviewEditActivity";

    public static final int CANVAS_WIDTH_MAX_SIZE = 3000;
    public static final int CANVAS_HEIGHT_MAX_SIZE = 3000;

    private static ClickState CLICK_STATE;

    private Canvas mCanvas;
    private PreviewEditActivity mActivity;
    private int canvasWidth, canvasHeight;
    public static int grandParentWidth, grandParentHeight;

    private ArrayList<PreviewItem> previewItems;
    private int previewPosWidth, previewPosHeight;
    private int previewWidth, previewHeight;

    private boolean isStampShown = false;
    private StampItem stampItem;
    private int stampWidth, stampHeight, stampPosWidthPer, stampPosHeightPer;
    private Uri stampURI;
    private Bitmap stampOriginalBitmap;
    private int stampWidthPos, stampHeightPos;
    private double stampRate;
    private StampAnchorEnum stampPosAnchor;

    private float stampGuideRectWidth = 5f;
    private float stampGuideLineWidth = 2f;
    private float stampGuideCircleRadius = 15f;

    private int movePrevX, movePrevY;

    private ProgressDialog saveProgressDialog;
    private ProgressDialog previewLoadProgressDialog;

    private boolean isSaveRoutine = false;
    private Snackbar saveInformationSnackbar;

    private PreviewBitmapControler pbc;


    public PreviewCanvasView(Context context, PreviewEditActivity activity, ArrayList<PreviewItem> previewItems, PreviewBitmapControler pbc) {
        super(context);
        this.mActivity = activity;
        this.previewItems = previewItems;
        this.pbc = pbc;
        CLICK_STATE = ClickState.getClickState();
        CLICK_STATE.start();

        saveProgressDialog = new ProgressDialog(mActivity);
        saveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        saveProgressDialog.setCancelable(false);
        saveProgressDialog.setCanceledOnTouchOutside(false);
        saveProgressDialog.setMessage(mActivity.getString(R.string.message_save_dialog));

        previewLoadProgressDialog = new ProgressDialog(mActivity);
        previewLoadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        previewLoadProgressDialog.setCancelable(false);
        previewLoadProgressDialog.setCanceledOnTouchOutside(false);
        previewLoadProgressDialog.setMessage(mActivity.getString(R.string.message_preview_load_dialog));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundGray));

        if (PreviewEditActivity.POSITION < 0) { // 프리뷰들중에서 아무런 프리뷰도 선택하지 않았을 때
            setBackgroundColor(Color.WHITE);
        } else {

            if (isSaveRoutine) {

                drawCanvasOriginalSize(PreviewEditActivity.POSITION);

            } else {

                drawBellowBitmap();
                if (isStampShown) {
                    drawStamp();
                    if (CLICK_STATE.isShowGuideLine()) {
                        drawStampEditGuide();
                    }
                }

            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setMeasuredDimension(CANVAS_WIDTH_MAX_SIZE, CANVAS_HEIGHT_MAX_SIZE);
    }

    public void previewValueInit() {
        previewPosWidth = 0;
        previewPosHeight = 0;
        previewWidth = 0;
        previewHeight = 0;
    }

    public boolean backPressed() {
        if (CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_STAMP_EDIT) {
            finishStampEdit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(event);
                break;
        }
        return true;
    }

    private void touchDown(MotionEvent event) {
        int x, y;
        x = (int) event.getX();
        y = (int) event.getY();

        movePrevX = x;
        movePrevY = y;

        if (isTouchInStamp(x, y) && isStampShown) {
            clickStamp();
        }
        if (isTouchStampZoom(x, y) && isStampShown) {
            clickStampToZoom();
        }

    }

    public void clickStamp() {
        CLICK_STATE.clickStamp();
        mActivity.editButtonGoneOrVisible(CLICK_STATE);
        invalidate();
    }

    public void clickStampToZoom() {
        CLICK_STATE.clickStampZoomStart();
        mActivity.editButtonGoneOrVisible(CLICK_STATE);
        invalidate();
    }

    public void stampReset() {
        stampWidthPos = (int) (previewWidth / 2.0f - stampWidth / 2.0f) + previewPosWidth;
        stampHeightPos = (int) (previewHeight / 2.0f - stampHeight / 2.0f) + previewPosHeight;
        stampItem.resetBrightness();
        invalidate();
    }

    private void touchMove(MotionEvent event) {
        int x, y;
        x = (int) event.getX();
        y = (int) event.getY();

        switch (CLICK_STATE.getClickStateEnum()) {
            case STATE_NONE_CLICK:
                break;

            case STATE_STAMP_EDIT:
                int deltaX = x - movePrevX;
                int deltaY = y - movePrevY;

                stampWidthPos += deltaX;
                stampHeightPos += deltaY;

                invalidate();
                break;

            case STATE_STAMP_ZOOM:
                double nowDist;
                double stampCenterX, stampCenterY;
                double newHeight, newWidth;

                stampCenterX = stampWidthPos + stampWidth / 2.0f;
                stampCenterY = stampHeightPos + stampHeight / 2.0f;

                nowDist = Math.sqrt(Math.pow(stampCenterX - x, 2) + Math.pow(stampCenterY - y, 2));

                newHeight = (2.0f * nowDist) / Math.sqrt((Math.pow(stampRate, 2) + 1));
                newWidth = newHeight * stampRate;

                stampWidthPos = (int) (stampCenterX - newWidth / 2);
                stampHeightPos = (int) (stampCenterY - newHeight / 2);
                stampWidth = (int) (stampCenterX + newWidth / 2) - stampWidthPos + 1;
                stampHeight = (int) (stampCenterY + newHeight / 2) - stampHeightPos + 1;

                invalidate();
                break;
        }

        movePrevX = x;
        movePrevY = y;

    }

    private void touchUp(MotionEvent event) {
        CLICK_STATE.clickStampZoomEnd();
    }

    private boolean isInBox(int x, int y, int x1, int y1, int x2, int y2) {
        if (x > x1 && x < x2 && y > y1 && y < y2) return true;
        else return false;
    }

    private boolean isInBoxWithWidth(int x, int y, int x1, int y1, int xWidth, int yWidth) {
        if (x > x1 && x < x1 + xWidth && y > y1 && y < y1 + yWidth) return true;
        else return false;
    }

    private boolean isInBoxWithRadius(int x, int y, int xCenter, int yCenter, int radius) {
        if ((x - xCenter) * (x - xCenter) + (y - yCenter) * (y - yCenter) < radius * radius)
            return true;
        else return false;
    }

    private boolean isTouchInStamp(int x, int y) {
        return isInBoxWithWidth(x, y, stampWidthPos, stampHeightPos, stampWidth, stampHeight);
    }

    private boolean isTouchStampZoom(int x, int y) {
        int radius = (int) (stampGuideCircleRadius + 25);
        int x_s = stampWidthPos; //x start
        int x_e = stampWidthPos + stampWidth; //x end
        int y_s = stampHeightPos; // y start
        int y_e = stampHeightPos + stampHeight; // y end

        if (isInBoxWithRadius(x, y, x_s, y_s, radius)
                || isInBoxWithRadius(x, y, x_s, y_e, radius)
                || isInBoxWithRadius(x, y, x_e, y_s, radius)
                || isInBoxWithRadius(x, y, x_e, y_e, radius)) {
            return true;
        } else {
            return false;
        }
    }

    private void drawCanvasOriginalSize(int previewPosition) {
        //Bitmap previewBitmap = previewItems.get(previewPosition).getmBitmap();
        Bitmap previewBitmap = pbc.getPreviewBitmap();
        int previewOrigBitmapWidth = previewBitmap.getWidth();
        int previewOrigBitmapHeight = previewBitmap.getHeight();

        Rect previewDst = new Rect(0, 0, previewOrigBitmapWidth, previewOrigBitmapHeight);
        mCanvas.drawBitmap(previewBitmap, null, previewDst, null);

        if (isStampShown) {
            int widthAnchor, heightAnchor, anchorInt;
            anchorInt = StampItem.stampAnchorToInt(stampPosAnchor);
            widthAnchor = anchorInt % 3;
            if (anchorInt == 0) heightAnchor = 0;
            else heightAnchor = StampItem.stampAnchorToInt(stampPosAnchor) / 3;

            int changedStampCenterX, changedStampCenterY, changedStampWidth, changedStampHeight;
            double widthRate = (double) stampItem.getWidth() / (double) previewWidth;
            double heightRate = (double) stampItem.getHeight() / (double) previewHeight;

            changedStampWidth = (int) (widthRate * previewOrigBitmapWidth);
            changedStampHeight = (int) (heightRate * previewOrigBitmapHeight);
            changedStampCenterX = (int) ((double) stampItem.getPos_width_per() * (double) previewOrigBitmapWidth / 100000d);
            changedStampCenterY = (int) ((double) stampItem.getPos_height_per() * (double) previewOrigBitmapHeight / 100000d);

            Paint paintContrastBrightness = getPaintContrastBrightnessPaint(1, stampItem.getAbsoluteBrightness());

            Rect stampDst = new Rect(
                    (int) (changedStampCenterX - changedStampWidth * widthAnchor / 2f),
                    (int) (changedStampCenterY - changedStampHeight * heightAnchor / 2f),
                    (int) (changedStampCenterX - changedStampWidth * widthAnchor / 2f) + changedStampWidth,
                    (int) (changedStampCenterY - changedStampHeight * heightAnchor / 2f) + changedStampHeight);
            Logger.d(TAG + "[SAVE]", changedStampCenterX + ", " + changedStampCenterY + ", " + changedStampWidth + ", " + changedStampHeight);
            mCanvas.drawBitmap(stampOriginalBitmap, null, stampDst, paintContrastBrightness);
        }
    }


    private void drawBellowBitmap() {
        //Bitmap previewBitmap = previewItems.get(PreviewEditActivity.POSITION).getmBitmap();
        Bitmap previewBitmap = pbc.getPreviewBitmap();
        canvasWidth = grandParentWidth - 16;
        canvasHeight = grandParentHeight - 16; // layout margin
        int previewBitmapWidth = previewBitmap.getWidth();
        int previewBitmapHeight = previewBitmap.getHeight();
        Logger.d(TAG, "CANVAS : W : " + canvasWidth + " , H : " + canvasHeight);

        double rate = (double) previewBitmapWidth / (double) previewBitmapHeight;
        Rect dst;
        double canvasRate = (double) canvasWidth / (double) canvasHeight;

        if (rate >= canvasRate && previewBitmapWidth >= canvasWidth) { // w > h

            previewPosWidth = 0;
            previewPosHeight = (canvasHeight - (int) (canvasWidth * (1 / rate))) / 2;
            previewWidth = canvasWidth;
            previewHeight = (int) (canvasWidth * (1 / rate));

        } else if (rate < canvasRate && previewBitmapHeight >= canvasHeight) { // w < h

            previewPosWidth = (canvasWidth - (int) (canvasHeight * (rate))) / 2;
            previewPosHeight = 0;
            previewWidth = (int) (canvasHeight * (rate));
            previewHeight = canvasHeight;

        } else {

            previewPosWidth = (canvasWidth - previewBitmapWidth) / 2;
            previewPosHeight = (canvasHeight - previewBitmapHeight) / 2;
            previewWidth = previewBitmapWidth;
            previewHeight = previewBitmapHeight;

        }
        dst = new Rect(previewPosWidth, previewPosHeight, previewPosWidth + previewWidth, previewPosHeight + previewHeight);
        Logger.d(TAG, previewPosWidth + "," + previewPosHeight + "," + (previewPosWidth + previewWidth) + "," + (previewPosHeight + previewHeight));
        mCanvas.drawBitmap(previewBitmap, null, dst, null);
    }

    private void drawStamp() {
        if (stampItem == null) {
            Logger.d(TAG, "no stamp item");
            return;
        }
        Logger.d(TAG, "STAMP draw : w, h, pw, ph, uri : " +
                stampWidth + ", " + stampHeight + ", " +
                stampPosWidthPer + ", " + stampPosHeightPer + ", " + stampURI);

        Paint paintContrastBrightness = getPaintContrastBrightnessPaint(1, stampItem.getAbsoluteBrightness());

        Rect dst = new Rect(stampWidthPos, stampHeightPos, stampWidthPos + stampWidth, stampHeightPos + stampHeight);
        mCanvas.drawBitmap(stampOriginalBitmap, null, dst, paintContrastBrightness);
    }

    public void onDrawStampBrigtness(int value) {
        if (stampItem == null) {
            return;
        }
        stampItem.setBrightness(value);
        callInvalidate();
    }

    public Paint getPaintContrastBrightnessPaint(float contrast, float brightness) {
        //contrast : 1, brightness : 0 is init value
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        return paint;
    }

    private void drawStampEditGuide() {
        int x_s = stampWidthPos; //x start
        int x_e = stampWidthPos + stampWidth; //x end
        int x_l = stampWidth; //x length
        int y_s = stampHeightPos; // y start
        int y_e = stampHeightPos + stampHeight; // y end
        int y_l = stampHeight; // y length

        Paint stampRect = new Paint();
        stampRect.setStrokeWidth(stampGuideRectWidth);
        stampRect.setColor(Color.WHITE);
        stampRect.setStyle(Paint.Style.STROKE);
        mCanvas.drawRect(x_s, y_s, x_e, y_e, stampRect);


        Paint stampGuideLine = new Paint();
        stampGuideLine.setStrokeWidth(stampGuideLineWidth);
        stampGuideLine.setColor(Color.WHITE);
        mCanvas.drawLine(x_s, y_l / 3.0f + y_s, x_e, y_l / 3.0f + y_s, stampGuideLine);
        mCanvas.drawLine(x_s, y_l * 2 / 3.0f + y_s, x_e, y_l * 2 / 3.0f + y_s, stampGuideLine);
        mCanvas.drawLine(x_l / 3.0f + x_s, y_s, x_l / 3.0f + x_s, y_e, stampGuideLine);
        mCanvas.drawLine(x_l * 2 / 3.0f + x_s, y_s, x_l * 2 / 3.0f + x_s, y_e, stampGuideLine);

        Paint stampGuideCircle = new Paint();
        stampGuideCircle.setColor(Color.WHITE);
        mCanvas.drawCircle(x_s, y_s, stampGuideCircleRadius, stampGuideCircle);
        mCanvas.drawCircle(x_s, y_e, stampGuideCircleRadius, stampGuideCircle);
        mCanvas.drawCircle(x_e, y_s, stampGuideCircleRadius, stampGuideCircle);
        mCanvas.drawCircle(x_e, y_e, stampGuideCircleRadius, stampGuideCircle);
    }

    public void finishStampEdit() {
        CLICK_STATE.clickFinishStampEdit();
        mActivity.editButtonGoneOrVisible(CLICK_STATE);

        int id = stampItem.getID();
        stampPosWidthPer = (int) (((stampWidth / 2.0f) + stampWidthPos - previewPosWidth) * 100000.0f / (previewWidth));
        stampPosHeightPer = (int) (((stampHeight / 2.0f) + stampHeightPos - previewPosHeight) * 100000.0f / (previewHeight));

        int widthAnchor = stampPosWidthPer / 33333;
        int heightAnchor = stampPosHeightPer / 33333;

        stampPosAnchor = StampItem.intToStampAnchor(widthAnchor + heightAnchor * 3);
        stampPosWidthPer = (int) (((stampWidth / 2.0f) * widthAnchor + stampWidthPos - previewPosWidth) * 100000.0f / (previewWidth));
        stampPosHeightPer = (int) (((stampHeight / 2.0f) * heightAnchor + stampHeightPos - previewPosHeight) * 100000.0f / (previewHeight));
        Logger.d(TAG + " [ANCHOR]", "ANCHOR : " + stampPosAnchor + "stampPosWidthPer" + stampPosWidthPer + "stampPosHeightPer" + stampPosHeightPer);

        stampItem.setWidth(stampWidth);
        stampItem.setHeight(stampHeight);

        stampItem.setPos_width_per(stampPosWidthPer);
        stampItem.setPos_height_per(stampPosHeightPer);

        stampItem.setPos_anchor(stampPosAnchor);

        mActivity.stampUpdate(id, stampWidth, stampHeight, stampPosWidthPer, stampPosHeightPer, StampItem.stampAnchorToInt(stampPosAnchor));

        invalidate();
    }

    public void deleteStamp() {
        CLICK_STATE.clickFinishStampEdit();
        mActivity.editButtonGoneOrVisible(CLICK_STATE);

        isStampShown = false;
        invalidate();
    }

    public void brightnessStamp() {
    }

    protected void showStamp() {
        setStampShown(true);
        CLICK_STATE.clickStampButton();
        previewItems.get(PreviewEditActivity.POSITION).editted();
        mActivity.editButtonGoneOrVisible(CLICK_STATE);
    }

    protected void callInvalidate() {
        invalidate();
    }

    protected boolean isStampShown() {
        return isStampShown;
    }

    protected void setStampShown(boolean stampShown) {
        isStampShown = stampShown;
    }

    public StampItem getStampItem() {
        return stampItem;
    }

    public void setStampItem(StampItem stampItem) {
        this.stampItem = stampItem;
        stampPosWidthPer = stampItem.getPos_width_per();
        stampPosHeightPer = stampItem.getPos_height_per();
        stampPosAnchor = stampItem.getPos_anchor();
        stampURI = stampItem.getImageURI();
        stampOriginalBitmap = stampURIToBitmap(stampURI, mActivity);

        //get init width and height when there is no information in db
        stampWidth = stampItem.getWidth();
        stampHeight = stampItem.getHeight();
        if (stampWidth < 0 || stampHeight < 0) {
            int id = stampItem.getID();
            stampWidth = stampOriginalBitmap.getWidth();
            stampHeight = stampOriginalBitmap.getHeight();
        }

        stampRate = (double) stampWidth / (double) stampHeight;

        //get correct position from anchor
        int widthAnchor, heightAnchor, anchorInt;
        anchorInt = StampItem.stampAnchorToInt(stampPosAnchor);
        widthAnchor = anchorInt % 3;
        if (anchorInt == 0) heightAnchor = 0;
        else heightAnchor = StampItem.stampAnchorToInt(stampPosAnchor) / 3;

        stampWidthPos = (int) ((stampPosWidthPer * previewWidth / 100000.0f) - (stampWidth * widthAnchor / 2.0f) + previewPosWidth);
        stampHeightPos = (int) ((stampPosHeightPer * previewHeight / 100000.0f) - (stampHeight * heightAnchor / 2.0f) + previewPosHeight);
        Logger.d(TAG + " [ANCHOR]", "ANCHOR : " + anchorInt + "stampPosWidthPer" + stampPosWidthPer + "stampPosHeightPer" + stampPosHeightPer);

        Logger.d(TAG, "STAMP set : w, h, pw, ph, uri : " +
                stampWidth + ", " + stampHeight + ", " +
                stampPosWidthPer + ", " + stampPosHeightPer + ", " + stampURI);
    }

    public Bitmap stampURIToBitmap(Uri imageUri, Activity activity) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
            return bitmap;
        } catch (IOException e) {
            Logger.d(TAG, "URI -> Bitmap : IOException" + imageUri);
            e.printStackTrace();
            return null;
        }
    }

    public void saveEnd() {
        isSaveRoutine = false;
        try {
            saveProgressDialog.dismiss();
        } catch (Exception e) {
        }
    }

    public void savePreviewEach(int nextPosition) {
        /**
         * if nextPosition == -1 , there is only click 'save',
         * or not, there is only click 'YES' in save question dialog.
         * 만약 nextPosition이 -1이면, 저장 버튼을 눌린것임.
         * 그렇지 않으면, "저장하시겠습니까" 에서 YES를 눌린 것임.
         */

        saveProgressDialog.show();
        changeCanvasToSave();
        SaveAllAsyncTask saveAllAsyncTask = new SaveAllAsyncTask();
        saveAllAsyncTask.execute(nextPosition);
    }

    public boolean isNowEditingStamp() {
        if (CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_NONE_CLICK) return false;
        else return true;
    }

    public void clickNewPreview(final int nextPosition) {
        AlertDialog.Builder stampDeleteAlert = new AlertDialog.Builder(mActivity);
        stampDeleteAlert.setMessage(mActivity.getString(R.string.snackbar_preview_edit_acti_clk_new_preview)).setCancelable(true)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                savePreviewEach(nextPosition);
                                //changeAndInitPreviewInCanvas(nextPosition);
                                return;
                            }
                        })
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                changeAndInitPreviewInCanvas(nextPosition);
                                return;
                            }
                        })
                .setNeutralButton("CANCLE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });


        if (PreviewEditActivity.POSITION != -1) {
            if (!previewItems.get(PreviewEditActivity.POSITION).getIsSaved()) {
                AlertDialog alert = stampDeleteAlert.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            } else {
                changeAndInitPreviewInCanvas(nextPosition);
            }
        }
    }

    public void changeAndInitPreviewInCanvas(int nextPosition) {
        //프리뷰를 다른걸 눌렀을때 캔버스의 프리뷰를 완전히 새로 바꿔주는 함수

        previewValueInit();
        PreviewEditActivity.POSITION = nextPosition;
        isStampShown = false;
        CLICK_STATE.finish();
        mActivity.editButtonGoneOrVisible(CLICK_STATE);
        previewItems.get(nextPosition).saved();

        //프리뷰 비트맵을 변경
        pbc.setPreviewBitmap(previewItems.get(nextPosition));

        invalidate();
        if (saveInformationSnackbar != null) {
            saveInformationSnackbar.dismiss();
        }
    }

    public void changeCanvasToSave() {
        isSaveRoutine = true;
        callInvalidate();
    }

    protected class SaveAllAsyncTask extends AsyncTask<Integer, Integer, String> {

        int nextPosition;
        final String ERROR_INVALID_POSITION = "ERROR_INVALID_POSITION";
        final String ERROR_IO_EXCEPTION = "ERROR_IO_EXCEPTION";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... param) {
            nextPosition = param[0];

            if (PreviewEditActivity.POSITION == -1) return ERROR_INVALID_POSITION;
            int previewPosition = PreviewEditActivity.POSITION;

            Bitmap screenshot = Bitmap.createBitmap(
                    //previewItems.get(previewPosition).getmBitmap().getWidth(),
                    //previewItems.get(previewPosition).getmBitmap().getHeight(),
                    pbc.getPreviewBitmap().getWidth(),
                    pbc.getPreviewBitmap().getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(screenshot);
            mActivity.getmPreviewCanvasView().draw(canvas);

            Uri resultUri = previewItems.get(previewPosition).getResultImageURI();
            String resultFilePath = resultUri.getPath();
            File resultFile = new File(resultFilePath);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(resultFile);
                screenshot.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(resultUri);
                mActivity.sendBroadcast(mediaScanIntent);

            } catch (IOException e) {
                e.printStackTrace();
                return ERROR_IO_EXCEPTION;
            }

            CLICK_STATE.clickSave();

            PreviewItem previewItem = previewItems.get(previewPosition);
            previewItem.setOriginalImageURI(resultUri);
            previewItem.saved();

            return resultFilePath;
        }

        @Override
        protected void onPostExecute(String str) {
            if (str != ERROR_INVALID_POSITION) {

                if (nextPosition != -1) {
                    changeAndInitPreviewInCanvas(nextPosition);
                } else {
                    changeAndInitPreviewInCanvas(PreviewEditActivity.POSITION);
                }


                if (str == ERROR_IO_EXCEPTION) {
                    saveInformationSnackbar = Snackbar.make(mActivity.getCurrentFocus(), "저장 실패!", Snackbar.LENGTH_LONG);
                    saveInformationSnackbar.show();
                } else {
                    File resultFile = new File(str);
                    saveInformationSnackbar = Snackbar.make(mActivity.getCurrentFocus(),
                            "저장 폴더 : " + MainActivity.PREVIEW_SAVED_DIRECTORY + "\n파일 이름 : " + resultFile.getName(),
                            Snackbar.LENGTH_LONG)
                            .setAction("NEXT", new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (PreviewEditActivity.POSITION + 1 < previewItems.size()) {
                                        nextPosition = PreviewEditActivity.POSITION + 1;
                                        changeAndInitPreviewInCanvas(nextPosition);
                                    }
                                }
                            });
                    saveInformationSnackbar.show();
                }
            }
            saveEnd();
            super.onPostExecute(str);
        }
    }
}
