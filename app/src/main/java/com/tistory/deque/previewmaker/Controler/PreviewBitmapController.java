package com.tistory.deque.previewmaker.Controler;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.tistory.deque.previewmaker.Model_PreviewData.PreviewItem;
import com.tistory.deque.previewmaker.Util.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PreviewBitmapController {
    private static PreviewBitmapController pbc;
    private Bitmap previewBitmap;
    private Bitmap blurredBitmap;
    private int bitmapWidth, bitmapHeight;
    private Activity mActivity;


    private PreviewBitmapController(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public static PreviewBitmapController getPreviewBitmapController(Activity mActivity) {
        //싱글턴 패턴
        if (pbc == null) {
            pbc = new PreviewBitmapController(mActivity);
        }
        return pbc;
    }

    public Bitmap getPreviewBitmap() {
        return previewBitmap;
    }

    public void setPreviewBitmap(PreviewItem previewItem) {
        this.previewBitmap = null;
        this.previewBitmap = previewItem.getBitmap();
        this.bitmapHeight = this.previewBitmap.getHeight();
        this.bitmapWidth = this.previewBitmap.getWidth();
        pbc.resetBlurBitmap();
    }

    private void resetBlurBitmap() {
        blurredBitmap = null;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public int getBitmapHeight() {
        return bitmapHeight;
    }

    private Bitmap roundedBitmapOval(Bitmap sourceBitmap, int left, int top, int right, int bottom, int ovalLeft, int ovalTop, int ovalRight, int ovalBottom) {
        final int width = sourceBitmap.getWidth();
        final int height = sourceBitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final RectF ovalRect = new RectF(ovalLeft - left, ovalTop - top, ovalRight - left, ovalBottom - top);
        Logger.d("MYTAG" , "rounded param : " + left + " , " + top  + " , " + right + " , " + bottom + " , " + ovalLeft  + " , " + ovalTop + " , " + ovalRight + " , " + ovalBottom);

        final Path path = new Path();
        path.addOval(ovalRect, Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(sourceBitmap, 0, 0, null);

        return outputBitmap;
    }

    /**
     * 비트맵의 부분을 블러링, 원본 비트맵을 기준으로 블러링함
     * left < right, top < bottom 이 여야함
     *
     * @param left   이하는 원본 비트맵을 기준으로 한 Oval의 값들. originalBlurOvalToResizedBlurOval()를 통해 구해옴
     * @param top
     * @param right
     * @param bottom
     */
    public void blurBitmapPart(int left, int top, int right, int bottom, int ovalLeft, int ovalTop, int ovalRight, int ovalBottom) {
        this.blurredBitmap = doFastBlur(Bitmap.createBitmap(this.previewBitmap, left, top, (right - left), (bottom - top)), 1, 50);
        if(this.blurredBitmap != null){
            this.blurredBitmap = roundedBitmapOval(this.blurredBitmap, left, top, right, bottom, ovalLeft, ovalTop, ovalRight, ovalBottom);
        }
    }

    public Bitmap getBlurredBitmap() {
        return blurredBitmap;
    }

    private Bitmap doFastBlur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Logger.d("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

}
