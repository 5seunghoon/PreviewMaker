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

    public static void resetGuideOvalRectF(float left, float top) {
        guideOvalRectF = new RectF();
        setGuideOvalRectFLeftTop(left, top);
        setGuideOvalRectFRightBottom(left, top);
    }

    private static void setGuideOvalRectFLeftTop(float left, float top) {
        guideOvalRectFLeft = left;
        guideOvalRectFTop = top;
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
    }

    public static RectF getGuideOvalRectF() {
        return guideOvalRectF;
    }

    /**
     * @param previewWidth
     * @param previewHeight
     * @param previewPosWidth
     * @param previewPosHeigth
     * @return 불가능한 타원(좌표가 하나라도 음수)일때 false반환
     */
    public static boolean cutOval(int previewWidth, int previewHeight, int previewPosWidth, int previewPosHeigth) {
        int left = Math.min((int) guideOvalRectFLeft, (int) guideOvalRectFRight);
        int top = Math.min((int) guideOvalRectFTop, (int) guideOvalRectFBottom);
        int right = Math.max((int) guideOvalRectFLeft, (int) guideOvalRectFRight);
        int bottom = Math.max((int) guideOvalRectFTop, (int) guideOvalRectFBottom);
        int b_left = previewWidth, b_top = previewHeight, b_right = previewPosWidth + previewWidth, b_bottom = previewPosHeigth + previewHeight; //비트맵의 좌표
        ArrayList<Integer> results = new ArrayList<>();

        if (!((left > b_right) || (top > b_bottom) || (right < b_left) || (bottom < b_top))) { // 사각형밖에 사각형이 있지 않을 경우
            left = Math.max(left, b_left);
            top = Math.max(top, b_top);
            right = Math.min(right, b_right);
            bottom = Math.min(bottom, b_bottom);
        }
        if (left < 0 || top < 0 || right < 0 || bottom < 0) {
            guideOvalRectFLeft = 0;
            guideOvalRectFTop = 0;
            guideOvalRectFRight = 0;
            guideOvalRectFBottom = 0;
            return false;
        }
        guideOvalRectFLeft = left;
        guideOvalRectFTop = top;
        guideOvalRectFRight = right;
        guideOvalRectFBottom = bottom;
        return true;
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
