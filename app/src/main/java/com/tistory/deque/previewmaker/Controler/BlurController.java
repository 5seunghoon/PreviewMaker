package com.tistory.deque.previewmaker.Controler;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;

import com.tistory.deque.previewmaker.Util.Logger;

public class BlurController {
    private static Paint blurGuidePaint;

    private static RectF guideOvalRectF;

    private static float guideOvalRectFLeft, guideOvalRectFTop, guideOvalRectFRight, guideOvalRectFBottom;

    public static void resetGuideOvalRectF(float left, float top){
        guideOvalRectF = new RectF();
        setGuideOvalRectFLeftTop(left, top);
        setGuideOvalRectFRightBottom(left, top);
    }

    private static void setGuideOvalRectFLeftTop(float left, float top){
        guideOvalRectFLeft = left;
        guideOvalRectFTop = top;
    }

    public static Pair<Float, Float> getGuideOvalRectFLeftTop(){
        return new Pair<>(guideOvalRectFLeft, guideOvalRectFTop);
    }
    public static Pair<Float, Float> getGuideOvalRectFRightBottom(){
        return new Pair<>(guideOvalRectFRight, guideOvalRectFBottom);
    }

    public static void setGuideOvalRectFRightBottom(float right, float bottom){
        guideOvalRectF.set(guideOvalRectFLeft, guideOvalRectFTop, right, bottom);
        guideOvalRectFRight = right;
        guideOvalRectFBottom = bottom;
    }

    public static RectF getGuideOvalRectF(){
        return guideOvalRectF;
    }


    public static Paint getBlurGuidePaint(){
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
