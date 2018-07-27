package com.tistory.deque.previewmaker.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.Toast;

import com.tistory.deque.previewmaker.Controler.BlurController;
import com.tistory.deque.previewmaker.Controler.PreviewBitmapController;
import com.tistory.deque.previewmaker.Controler.RetouchingPaintController;
import com.tistory.deque.previewmaker.Model_Global.ClickState;
import com.tistory.deque.previewmaker.Model_Global.ClickStateEnum;
import com.tistory.deque.previewmaker.Model_Global.SeekBarSelectedEnum;
import com.tistory.deque.previewmaker.Model_PreviewData.PreviewItem;
import com.tistory.deque.previewmaker.R;
import com.tistory.deque.previewmaker.Model_StampData.StampAnchorEnum;
import com.tistory.deque.previewmaker.Model_StampData.StampItem;
import com.tistory.deque.previewmaker.Util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PreviewCanvasView extends View {
    private final static String TAG = "PreviewEditActivity";

    public static final int CANVAS_WIDTH_MAX_SIZE = 3000;
    public static final int CANVAS_HEIGHT_MAX_SIZE = 3000;

    public static int grandParentLayoutMargin = 16;

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
    private ProgressDialog blurringBitmapProgressDialog;

    private boolean isSaveRoutine = false;
    private boolean isLoadRoutine = false;
    private boolean isBlurringRoutine = false;
    private boolean isSaveRoutineWithBlur = false;
    //스탬프와 함께 저장해야 하는지에 대한 boolean. 만약 프리뷰'만' 저장하는 경우(간단보정같이) 이걸 false로 해야함
    private Snackbar saveInformationSnackbar;

    private PreviewBitmapController pbc;

    private boolean isSaveReady = false;


    public PreviewCanvasView(Context context, PreviewEditActivity activity, ArrayList<PreviewItem> previewItems, PreviewBitmapController pbc) {
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

        blurringBitmapProgressDialog = new ProgressDialog(mActivity);
        blurringBitmapProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        blurringBitmapProgressDialog.setCancelable(false);
        blurringBitmapProgressDialog.setCanceledOnTouchOutside(false);
        blurringBitmapProgressDialog.setMessage(mActivity.getString(R.string.message_blurring_bitmap_dialog));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundGray));

        if (getPosition() < 0) { // 프리뷰들중에서 아무런 프리뷰도 선택하지 않았을 때
            setBackgroundColor(Color.WHITE);
        } else {

            if (isSaveRoutine) {

                drawCanvasOriginalSize(getPosition(), isSaveRoutineWithBlur);

            } else if (!isLoadRoutine) {

                drawBellowBitmap();

                if (!isBlurringRoutine) {
                    if (CLICK_STATE.isBlurGuide()) {
                        //블러 가이드 그리기
                        //블러를 그리는 중일경우, 비트맵 먼저 그리고 -> 블러 그리고 -> 낙관 그림
                        drawBlurGuide();
                    }

                    if (CLICK_STATE.isBlur()) {
                        // 블러 그리기
                        drawBlur();
                    }

                    if (isStampShown) {
                        //낙관 그리기
                        drawStamp();
                        if (CLICK_STATE.isShowGuideLine()) {
                            drawStampEditGuide();
                        }
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

    private int getPosition() {
        return PreviewEditActivity.POSITION;
    }

    private void setPosition(int nextPosition) {
        PreviewEditActivity.POSITION = nextPosition;
    }

    public void setIsSaveReady(boolean value) {
        isSaveReady = true;
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
        if (CLICK_STATE.isBlur()) {
            touchDownForBlur(event);
        }
        if (CLICK_STATE.isBlurGuide()) {
            touchDownForBlurGuide(event);
        } else {
            touchDownForStamp(event);
        }
    }

    private void touchMove(MotionEvent event) {
        if (CLICK_STATE.isBlurGuide()) {
            touchMoveForBlurGuide(event);
        } else {
            touchMoveForStamp(event);
        }
    }

    private void touchUp(MotionEvent event) {
        if (CLICK_STATE.isBlurGuide()) {
            touchUpForBlurGuide(event);
        } else {
            touchUpForStamp(event);
        }
    }

    private void touchDownForBlur(MotionEvent event) {
        CLICK_STATE.restartBlurGuide();
    }

    private void touchDownForBlurGuide(MotionEvent event) {
        float x, y;
        x = event.getX();
        y = event.getY();
        BlurController.resetGuideOvalRectF(x, y);
        invalidate();
    }

    private void touchMoveForBlurGuide(MotionEvent event) {
        float x, y;
        x = event.getX();
        y = event.getY();
        BlurController.setGuideOvalRectFRightBottom(x, y);
        invalidate();
    }

    private void touchUpForBlurGuide(MotionEvent event) {
        //여기에 타원 영역 블러해서 비트맵 위에 그리기
        CLICK_STATE.endBlurGuide(); //Blur guide -> blur
        boolean isValidOval = BlurController.cutOval(previewWidth, previewHeight, previewPosWidth, previewPosHeight);
        if(!isValidOval){
            CLICK_STATE.clickFinishBlur(); // blur or guide -> filter
            invalidate();
        } else {
            makeOvalBlur();
            invalidate();
        }
    }

    private void touchDownForStamp(MotionEvent event) {
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

    private void touchMoveForStamp(MotionEvent event) {
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

    private void touchUpForStamp(MotionEvent event) {
        CLICK_STATE.clickStampZoomEnd();
    }

    public void clickStamp() {
        CLICK_STATE.clickStamp();
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);
        invalidate();
    }

    public void clickStampToZoom() {
        CLICK_STATE.clickStampZoomStart();
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);
        invalidate();
    }

    public void stampReset() {
        stampWidthPos = (int) (previewWidth / 2.0f - stampWidth / 2.0f) + previewPosWidth;
        stampHeightPos = (int) (previewHeight / 2.0f - stampHeight / 2.0f) + previewPosHeight;
        stampItem.resetBrightness();
        invalidate();
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

    /**
     * @return partLeft, partTop, partRight, partBottom
     */
    private ArrayList<Double> originalBlurOvalToResizedBlurOval() {
        float left = Math.min(BlurController.getGuideOvalRectFLeftTop().first, BlurController.getGuideOvalRectFRightBottom().first);
        float top = Math.min(BlurController.getGuideOvalRectFLeftTop().second, BlurController.getGuideOvalRectFRightBottom().second);
        float right = Math.max(BlurController.getGuideOvalRectFLeftTop().first, BlurController.getGuideOvalRectFRightBottom().first);
        float bottom = Math.max(BlurController.getGuideOvalRectFLeftTop().second, BlurController.getGuideOvalRectFRightBottom().second);

        int pbw = pbc.getBitmapWidth();
        int pbh = pbc.getBitmapHeight();
        ArrayList<Integer> elements = getResizedBitmapElements(pbw, pbh, grandParentWidth, grandParentHeight, grandParentLayoutMargin);
        ArrayList<Double> results = new ArrayList<>();

        double pwPos = elements.get(0);
        double phPos = elements.get(1);
        double pw = elements.get(2);
        double ph = elements.get(3);

        double widthRate = pbw / pw;
        double heightRate = pbh / ph;

        double partLeft = widthRate * (left - pwPos);
        double partTop = heightRate * (top - phPos);
        double partRight = widthRate * (right - pwPos);
        double partBottom = heightRate * (bottom - phPos);

        results.add(partLeft);
        results.add(partTop);
        results.add(partRight);
        results.add(partBottom);

        return results;
    }

    private void drawCanvasOriginalSize(int previewPosition, boolean isSaveRoutineWithBlur) {
        //Bitmap previewBitmap = previewItems.get(previewPosition).getmBitmap();
        Bitmap previewBitmap = pbc.getPreviewBitmap();
        int previewOrigBitmapWidth = pbc.getBitmapWidth();
        int previewOrigBitmapHeight = pbc.getBitmapHeight();

        Rect previewDst = new Rect(0, 0, previewOrigBitmapWidth, previewOrigBitmapHeight);

        PreviewItem nowPreview = previewItems.get(previewPosition);
        Paint paintPreviewContrastBrightness = RetouchingPaintController.getPaint(
                nowPreview.getContrastForFilter()
                , nowPreview.getBrightnessForFilter()
                , nowPreview.getSaturationForFilter()
                , nowPreview.getKelvinForFilter()
        );
        mCanvas.drawBitmap(previewBitmap, null, previewDst, paintPreviewContrastBrightness);

        if (isSaveRoutineWithBlur) {
            ArrayList<Double> partOvalElements = originalBlurOvalToResizedBlurOval();

            Rect blurRect = new Rect(partOvalElements.get(0).intValue(), partOvalElements.get(1).intValue(), partOvalElements.get(2).intValue(), partOvalElements.get(3).intValue());
            mCanvas.drawBitmap(pbc.getBlurredBitmap(), null, blurRect, paintPreviewContrastBrightness);
        } else if (isStampShown) {
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

            Paint paintContrastBrightness = RetouchingPaintController.getPaint(1, stampItem.getAbsoluteBrightness(), 1, 1);

            Rect stampDst = new Rect(
                    (int) (changedStampCenterX - changedStampWidth * widthAnchor / 2f),
                    (int) (changedStampCenterY - changedStampHeight * heightAnchor / 2f),
                    (int) (changedStampCenterX - changedStampWidth * widthAnchor / 2f) + changedStampWidth,
                    (int) (changedStampCenterY - changedStampHeight * heightAnchor / 2f) + changedStampHeight);
            Logger.d(TAG + "[SAVE]", changedStampCenterX + ", " + changedStampCenterY + ", " + changedStampWidth + ", " + changedStampHeight);
            mCanvas.drawBitmap(stampOriginalBitmap, null, stampDst, paintContrastBrightness);
        }

        isSaveReady = true;
    }

    /**
     * @return previewPosWidth, previewPosHeight, previewWidth, previewHeight
     */
    private ArrayList<Integer> getResizedBitmapElements(int previewBitmapWidth, int previewBitmapHeight, int grandParentWidth, int grandParentHeight, int grandParentLayoutMargin) {
        canvasWidth = grandParentWidth - grandParentLayoutMargin;
        canvasHeight = grandParentHeight - grandParentLayoutMargin; // layout margin
        Logger.d(TAG, "CANVAS : W : " + canvasWidth + " , H : " + canvasHeight);

        double rate = (double) previewBitmapWidth / (double) previewBitmapHeight;
        double canvasRate = (double) canvasWidth / (double) canvasHeight;

        int previewPosWidth;
        int previewPosHeight;
        int previewWidth;
        int previewHeight;

        ArrayList<Integer> elements = new ArrayList<>();

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

        elements.add(previewPosWidth);
        elements.add(previewPosHeight);
        elements.add(previewWidth);
        elements.add(previewHeight);

        return elements;
    }

    private void drawBellowBitmap() {
        //Bitmap previewBitmap = previewItems.get(getPosition()).getmBitmap();
        Bitmap previewBitmap = pbc.getPreviewBitmap();
        int previewBitmapWidth = pbc.getBitmapWidth();
        int previewBitmapHeight = pbc.getBitmapHeight();
        Rect dst;
        ArrayList<Integer> elements = getResizedBitmapElements(previewBitmapWidth, previewBitmapHeight, grandParentWidth, grandParentHeight, grandParentLayoutMargin);

        previewPosWidth = elements.get(0);
        previewPosHeight = elements.get(1);
        previewWidth = elements.get(2);
        previewHeight = elements.get(3);
        dst = new Rect(previewPosWidth, previewPosHeight, previewPosWidth + previewWidth, previewPosHeight + previewHeight);

        PreviewItem nowPreview = previewItems.get(getPosition());
        Paint paintPreviewContrastBrightness = RetouchingPaintController.getPaint(
                nowPreview.getContrastForFilter()
                , nowPreview.getBrightnessForFilter()
                , nowPreview.getSaturationForFilter()
                , nowPreview.getKelvinForFilter()
        );

        Logger.d(TAG, previewPosWidth + "," + previewPosHeight + "," + (previewPosWidth + previewWidth) + "," + (previewPosHeight + previewHeight));
        try {
            mCanvas.drawBitmap(previewBitmap, null, dst, paintPreviewContrastBrightness);
        } catch (NullPointerException e) {
            Toast.makeText(mActivity.getApplicationContext(), "NullPointerException: canvas.drawBitmap", Toast.LENGTH_LONG).show();
        }
    }

    private void drawBlurGuide() {
        Logger.d("MYTAG", "drawBlurGuide()");
        /**
         * 블러의 가이드를 그림
         *
         * 1. 타원을 사용자가 설정하면
         * 5. OK를 눌리면 바로 블러링 연산
         * 2. 그 타원을 포함하는 사각형을 구하고
         * 3. 그 사각형만큼 비트맵을 블러 적용
         * 4. 그 후 그 블러된 비트맵을 pbc.blurBitmap에 저장
         */
        mCanvas.drawOval(BlurController.getGuideOvalRectF(), BlurController.getBlurGuidePaint());
    }

    private void drawBlur() {
        /**
         * pbc의 블러 비트맵(타원형)을 캔버스에 그림
         */
        if (pbc.getBlurredBitmap() != null) {
            float left = Math.min(BlurController.getGuideOvalRectFLeftTop().first, BlurController.getGuideOvalRectFRightBottom().first);
            float top = Math.min(BlurController.getGuideOvalRectFLeftTop().second, BlurController.getGuideOvalRectFRightBottom().second);
            float right = Math.max(BlurController.getGuideOvalRectFLeftTop().first, BlurController.getGuideOvalRectFRightBottom().first);
            float bottom = Math.max(BlurController.getGuideOvalRectFLeftTop().second, BlurController.getGuideOvalRectFRightBottom().second);

            PreviewItem nowPreview = previewItems.get(getPosition());
            Paint paintPreviewContrastBrightness = RetouchingPaintController.getPaint(
                    nowPreview.getContrastForFilter()
                    , nowPreview.getBrightnessForFilter()
                    , nowPreview.getSaturationForFilter()
                    , nowPreview.getKelvinForFilter()
            );

            Rect blurredBitmapRect = new Rect((int) left, (int) top, (int) right, (int) bottom);
            mCanvas.drawBitmap(pbc.getBlurredBitmap(), null, blurredBitmapRect, paintPreviewContrastBrightness);
        }
    }

    private void makeOvalBlur() {
        /**
         * 타원영역만큼 비트맵을 블러하여 pbc에 저장해둠
         */
        isBlurringRoutine = true;
        blurringBitmapProgressDialog.show();
        BlurringBitmapAsyncTask blurringBitmapAsyncTask = new BlurringBitmapAsyncTask();
        blurringBitmapAsyncTask.execute(getPosition());
    }

    private void drawStamp() {
        if (stampItem == null) {
            Logger.d(TAG, "no stamp item");
            return;
        }
        Logger.d(TAG, "STAMP draw : w, h, pw, ph, uri : " +
                stampWidth + ", " + stampHeight + ", " +
                stampPosWidthPer + ", " + stampPosHeightPer + ", " + stampURI);

        Paint paintContrastBrightness = RetouchingPaintController.getPaint(1, stampItem.getAbsoluteBrightness(), 1, 1);

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

    public void onDrawPreviewColorParam(int value, SeekBarSelectedEnum sem) {
        /**
         * 프리뷰의 밝기, 대비, 색온도를 변경
         */
        if (previewItems == null) {
            return;
        }
        if (previewItems.get(getPosition()) == null) {
            return;
        }

        switch (sem) {
            case BRIGHTNESS:
                break;
            case CONTRAST:
                break;
            case PREVIEW_BRIGHTNESS:
                previewItems.get(getPosition()).setBrightness(value);
                break;
            case PREVIEW_KELVIN:
                previewItems.get(getPosition()).setKelvin(value);
                break;
            case PREVIEW_CONTRAST:
                previewItems.get(getPosition()).setContrast(value);
                break;
            case PREVIEW_SATURATION:
                previewItems.get(getPosition()).setSaturation(value);
                break;
        }

        callInvalidate();
    }

    public void clickFilterEditStart() {
        CLICK_STATE.clickFilterButton();
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);
    }

    public void finishPreviewEdit() {
        //CLICK_STATE.clickFinishFilterEdit();
        CLICK_STATE.clickFinishFilterEdit();
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);
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
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);

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
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);

        isStampShown = false;
        invalidate();
    }


    protected void showStamp() {
        setStampShown(true);
        CLICK_STATE.clickStampButton();
        previewItems.get(getPosition()).editted();
        mActivity.editButtonInvisibleOrVisible(CLICK_STATE);
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
        isSaveReady = false;
        isSaveRoutineWithBlur = false;
        previewItems.get(getPosition()).resetFilterValue();
        try {
            saveProgressDialog.dismiss();
        } catch (Exception e) {
        }
        callInvalidate();
    }

    public void savePreviewEach(int nextPosition) {
        /**
         * if nextPosition == -1 , there is only click 'save',
         * or not, there is only click 'YES' in save question dialog.
         * 만약 nextPosition이 -1이면, 저장 버튼을 눌린것임.
         * 그렇지 않으면, "저장하시겠습니까" 에서 YES를 눌린 것임.
         */
        if (isLoadRoutine) return;

        isSaveRoutine = true;

        //isSaveToSimplePreviewFilter가 true라는 말은 간단 보정이라는 말임
        //그말은 스탬프랑 같이 저장하는게 아니라는 말(false)임

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
        stampDeleteAlert
                .setMessage(mActivity.getString(R.string.snackbar_preview_edit_acti_clk_new_preview)).setCancelable(true)
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


        if (getPosition() != -1) {
            if (!previewItems.get(getPosition()).getIsSaved()) {
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
        if (isSaveRoutine) return;

        if (isSaveRoutineWithBlur) {
            previewValueInit();
            setPosition(nextPosition);
            mActivity.editCancelBtnInvisible();
            mActivity.seekbarInvisibleAllBtnVisible();
            changeStartPreviewBitmap();
        } else {
            previewItems.get(nextPosition).resetFilterValue();
            previewValueInit();

            isStampShown = false;
            setPosition(nextPosition);
            CLICK_STATE.finish();
            mActivity.editButtonInvisibleOrVisible(CLICK_STATE);

            //프리뷰 비트맵을 변경
            changeStartPreviewBitmap();

            if (saveInformationSnackbar != null) saveInformationSnackbar.dismiss();
        }
    }


    public void changeCanvasToSave() {
        callInvalidate();
    }

    public void cropPreview() {
        //프리뷰를 잘랐을때 마지막으로 호출되는 함수

        changeStartPreviewBitmap();
    }

    public void changeStartPreviewBitmap() {
        //프리뷰 비트맵을 변경하기 위해 비동기 호출
        isLoadRoutine = true;
        previewLoadProgressDialog.show();
        LoadPreviewAsyncTask loadPreviewAsyncTask = new LoadPreviewAsyncTask();
        loadPreviewAsyncTask.execute(0);
    }

    public void changeProgressPreviewBitmap() {
        //프리뷰 비트맵을 바꿈
        pbc.setPreviewBitmap(previewItems.get(getPosition()));
    }


    public void changeSuccessPreviewBitmap() {
        try {
            previewLoadProgressDialog.dismiss();
        } catch (Exception e) {
        }
        isLoadRoutine = false;
        callInvalidate();
        //mActivity.doClickButtonStamp();
    }

    public void okBlurEdit() {
        if (CLICK_STATE.isBlur()) {
            //블러링 되어있는 비트맵을 포함하는 캔버스를 저장함
            isSaveRoutineWithBlur = true;
            savePreviewEach(-1);
        }
        CLICK_STATE.clickFinishBlur();
    }

    public void cancelBlurEdit() {
        CLICK_STATE.clickFinishBlur();
        //캔버스에서 블러링되어있는 비트맵을 빼고 다시 그림
        isSaveRoutineWithBlur = false;
        invalidate();
    }

    public void clickBlurButton() {
        CLICK_STATE.clickBlurButton();
    }

    public void finishBlurring() {
        try {
            blurringBitmapProgressDialog.dismiss();
        } catch (Exception e) {
        }
        isBlurringRoutine = false;
        callInvalidate();
    }


    protected class BlurringBitmapAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            ArrayList<Double> partOvalElements = originalBlurOvalToResizedBlurOval();
            pbc.blurBitmapPart(partOvalElements.get(0).intValue(), partOvalElements.get(1).intValue(), partOvalElements.get(2).intValue(), partOvalElements.get(3).intValue());
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            finishBlurring();
        }
    }

    protected class LoadPreviewAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            changeProgressPreviewBitmap();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            changeSuccessPreviewBitmap();
        }

    }

    protected class SaveAllAsyncTask extends AsyncTask<Integer, Integer, String> {

        int nextPosition;
        final String ERROR_INVALID_POSITION = "ERROR_INVALID_POSITION";
        final String ERROR_IO_EXCEPTION = "ERROR_IO_EXCEPTION";

        SaveAllAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... param) {
            while (!isSaveReady) {
                //spin ready to save
                //캔버스에 오리지널 크기로 비트맵을 다 그리고 나면 save ready가 true가 됨
            }
            nextPosition = param[0];

            if (getPosition() == -1) return ERROR_INVALID_POSITION;
            int previewPosition = getPosition();

            Bitmap screenshot = Bitmap.createBitmap(
                    pbc.getBitmapWidth(),
                    pbc.getBitmapHeight(),
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
                isSaveRoutine = false;

                if (nextPosition != -1) {
                    changeAndInitPreviewInCanvas(nextPosition);
                    previewItems.get(nextPosition).saved();
                } else {
                    changeAndInitPreviewInCanvas(getPosition());
                }


                if (str == ERROR_IO_EXCEPTION) {
                    saveInformationSnackbar = Snackbar.make(mActivity.getCurrentFocus(), "저장 실패!", Snackbar.LENGTH_LONG);
                    saveInformationSnackbar.show();
                } else {
                    File resultFile = new File(str);
                    saveInformationSnackbar = Snackbar.make(mActivity.getCurrentFocus(),
                            "저장 폴더 : " + MainActivity.PREVIEW_SAVED_DIRECTORY + "\n파일 이름 : " + resultFile.getName(),
                            Snackbar.LENGTH_LONG);
                    if (nextPosition == -1) {
                        saveInformationSnackbar.setAction("NEXT", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getPosition() + 1 < previewItems.size()) {
                                    nextPosition = getPosition() + 1;
                                    changeAndInitPreviewInCanvas(nextPosition);
                                }
                            }
                        });
                    }
                    //저장 위치 등을 알려주는 스낵바
                    saveInformationSnackbar.show();

                }
            }
            saveEnd();
        }
    }
}
