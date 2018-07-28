package com.tistory.deque.previewmaker.Controler;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;

import com.tistory.deque.previewmaker.Util.Logger;

import java.util.ArrayList;

public class BlurController {
    private static Paint blurGuidePaint;

    private static RectF guideOvalRectF;

    private static float guideOvalRectFLeft, guideOvalRectFTop, guideOvalRectFRight, guideOvalRectFBottom;
    private static float guideOvalRectFLeftOrig, guideOvalRectFTopOrig, guideOvalRectFRightOrig, guideOvalRectFBottomOrig;

    public static void resetGuideOvalRectF(float left, float top) {
        guideOvalRectF = new RectF();
        setGuideOvalRectFLeftTop(left, top);
        setGuideOvalRectFRightBottom(left, top);
    }

    private static void setGuideOvalRectFLeftTop(float left, float top) {
        guideOvalRectFLeft = left;
        guideOvalRectFTop = top;
        guideOvalRectFLeftOrig = left;
        guideOvalRectFTopOrig = top;
    }

    public static Pair<Float, Float> getGuideOvalRectFLeftTop() {
        return new Pair<>(guideOvalRectFLeft, guideOvalRectFTop);
    }

    public static Pair<Float, Float> getGuideOvalRectFRightBottom() {
        return new Pair<>(guideOvalRectFRight, guideOvalRectFBottom);
    }

    public static void setGuideOvalRectFRightBottom(float right, float bottom) {
        guideOvalRectF.set(guideOvalRectFLeft, guideOvalRectFTop, right, bottom);
        guideOvalRectFRight = right;
        guideOvalRectFBottom = bottom;
        guideOvalRectFRightOrig = right;
        guideOvalRectFBottomOrig = bottom;
    }

    public static RectF getGuideOvalRectF() {
        return guideOvalRectF;
    }

    /**
     * 자르기 전 타원의 좌표
     * @return
     */
    public static ArrayList<Float> getGuideOvalRectFOrig(){
        ArrayList<Float> result = new ArrayList<>();
        float leftOrig = Math.min(guideOvalRectFLeftOrig, guideOvalRectFRightOrig);
        float topOrig = Math.min(guideOvalRectFTopOrig, guideOvalRectFBottomOrig);
        float rightOrig = Math.max(guideOvalRectFLeftOrig, guideOvalRectFRightOrig);
        float bottomOrig = Math.max(guideOvalRectFTopOrig, guideOvalRectFBottomOrig);

        guideOvalRectFLeftOrig = leftOrig;
        guideOvalRectFTopOrig = topOrig;
        guideOvalRectFRightOrig = rightOrig;
        guideOvalRectFBottomOrig = bottomOrig;

        result.add(guideOvalRectFLeftOrig);
        result.add(guideOvalRectFTopOrig);
        result.add(guideOvalRectFRightOrig);
        result.add(guideOvalRectFBottomOrig);

        return result;
    }

    /**
     * @param previewWidth
     * @param previewHeight
     * @param previewPosWidth
     * @param previewPosHeigth
     * @return 불가능한 타원(좌표가 하나라도 음수)일때 false반환
     */
    public static boolean cutOval(int previewWidth, int previewHeight, int previewPosWidth, int previewPosHeigth) {
        //불가능한 타원을 만들면 앱이 터짐
        int left = Math.min((int) guideOvalRectFLeft, (int) guideOvalRectFRight);
        int top = Math.min((int) guideOvalRectFTop, (int) guideOvalRectFBottom);
        int right = Math.max((int) guideOvalRectFLeft, (int) guideOvalRectFRight);
        int bottom = Math.max((int) guideOvalRectFTop, (int) guideOvalRectFBottom);
        int b_left = previewPosWidth, b_top = previewPosHeigth, b_right = previewPosWidth + previewWidth, b_bottom = previewPosHeigth + previewHeight; //비트맵의 좌표

        Logger.d("MYTAG", "CUT OVAL, BITMAP : "+ b_left + ", " + b_top + ", " + b_right + ", " + b_bottom);
        Logger.d("MYTAG", "OVAL : " + left + ", " + top + ", " + right+ ", " +bottom);

        if((left == right) || (top == bottom)){ // 4. 타원이 넓이가 없을때
            guideOvalRectFLeft = 0;
            guideOvalRectFTop = 0;
            guideOvalRectFRight = 1;
            guideOvalRectFBottom = 1;
            Logger.d("MYTAG", "CASE 4 FALSE : " + left + ", " + top + ", " + right+ ", " +bottom);
            return false;
        }

        if((b_left <= left && b_top <= top && b_right >= right && b_bottom >= bottom)){ // 1. 타원이 비트맵 내부
            guideOvalRectFLeft = left;
            guideOvalRectFTop = top;
            guideOvalRectFRight = right;
            guideOvalRectFBottom = bottom;
            Logger.d("MYTAG", "CASE 1 TRUE : " + left+ ", " +top+ ", " +right+ ", " +bottom);
            return true;
        } else if(b_right < left || b_left > right || b_bottom < top || b_top > bottom) { // 2. 타원이 비트맵 바깥
            guideOvalRectFLeft = 0;
            guideOvalRectFTop = 0;
            guideOvalRectFRight = 1;
            guideOvalRectFBottom = 1;
            Logger.d("MYTAG", "CASE 2 FALSE : " + left + ", " + top + ", " + right+ ", " +bottom);
            return false;
        } else { // 3. 그 외 겹칠 경우
            guideOvalRectFLeft = Math.max(left, b_left);;
            guideOvalRectFTop = Math.max(top, b_top);;
            guideOvalRectFRight = Math.min(right, b_right);;
            guideOvalRectFBottom = Math.min(bottom, b_bottom);;
            Logger.d("MYTAG", "CASE 3 TRUE : " + guideOvalRectFLeft + ", " + guideOvalRectFTop + ", " + guideOvalRectFRight+ ", " +guideOvalRectFBottom);
            return true;
        }
    }


    public static Paint getBlurGuidePaint() {
        blurGuidePaint = new Paint();
        /*
        blurGuidePaint.setStyle(Paint.Style.STROKE);
        blurGuidePaint.setStrokeJoin(Paint.Join.ROUND);
        blurGuidePaint.setStrokeCap(Paint.Cap.ROUND);
        blurGuidePaint.setStrokeWidth(blurPaintRadius);
        blurGuidePaint.setAntiAlias(true);
        blurGuidePaint.setFilterBitmap(false);
        */

        blurGuidePaint.setColor(Color.MAGENTA);
        blurGuidePaint.setAlpha(80);
        blurGuidePaint.setDither(true);
        blurGuidePaint.setStrokeJoin(Paint.Join.ROUND);
        blurGuidePaint.setStrokeCap(Paint.Cap.ROUND);
        blurGuidePaint.setStyle(Paint.Style.FILL);

        //blurGuidePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        //BlurMaskFilter blur = new BlurMaskFilter(100, BlurMaskFilter.Blur.NORMAL);
        //blurGuidePaint.setMaskFilter(blur);

        return blurGuidePaint;
    }

}
