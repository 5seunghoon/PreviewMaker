package com.tistory.deque.previewmaker.Controler;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.tistory.deque.previewmaker.Util.Logger;

public class BlurControler {
    public static Path blurPath = new Path();
    private static Paint blurPaint;
    private static float blurPaintRadius = 50.0f;
    public static int blurPaintRadiusMax = 200;

    private static float prevX, prevY;

    public static void setPrevXY(float x, float y) {
        prevX = x;
        prevY = y;
    }

    public static float getBlurPaintRadius(){
        return blurPaintRadius;
    }
    public static float getBlurPaintRadiusSquare() {
        return blurPaintRadius * blurPaintRadius;
    }
    public static void setBlurPaintRadius(float radius){
        blurPaintRadius = radius;
        Logger.d("MYTAG", radius + " : RADIUS");
        if(blurPaint != null){
            blurPaint.setStrokeWidth(blurPaintRadius);
        }
    }

    public static float getPrevX(){
        return prevX;
    }

    public static float getPrevY(){
        return prevY;
    }

    public static Paint getBlurPaint(){
        blurPaint = new Paint();
        /*
        blurPaint.setStyle(Paint.Style.STROKE);
        blurPaint.setStrokeJoin(Paint.Join.ROUND);
        blurPaint.setStrokeCap(Paint.Cap.ROUND);
        blurPaint.setStrokeWidth(blurPaintRadius);
        blurPaint.setAntiAlias(true);
        blurPaint.setFilterBitmap(false);
        */

        blurPaint.setColor(Color.MAGENTA);
        blurPaint.setAlpha(100);
        blurPaint.setStrokeWidth(blurPaintRadius);
        blurPaint.setDither(true);
        blurPaint.setStrokeJoin(Paint.Join.ROUND);
        blurPaint.setStyle(Paint.Style.STROKE);
        blurPaint.setStrokeCap(Paint.Cap.ROUND);

        //blurPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        BlurMaskFilter blur = new BlurMaskFilter(100, BlurMaskFilter.Blur.NORMAL);
        blurPaint.setMaskFilter(blur);

        return blurPaint;
    }
}
