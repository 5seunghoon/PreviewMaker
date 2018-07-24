package com.tistory.deque.previewmaker.Controler;

import android.graphics.BlurMaskFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.tistory.deque.previewmaker.Util.Logger;

public class PreviewPaintControler {

    public static Paint getPaint(float contrast, float brightness, float saturation, float kelvin){
        //contrast : 1, brightness : 0, saturation : 1 is init value
        //https://docs.rainmeter.net/tips/colormatrix-guide/
        float sr = (1 - saturation) * 0.2125f;
        float sg = (1 - saturation) * 0.7154f;
        float sb = (1 - saturation) * 0.0721f;

        float kr = (1 - kelvin) * 0.07f;
        float kg = (1 - kelvin) * 0.33f;
        float kb = (1 - kelvin) * 0.6f;

        float t = (1.0f - contrast) * 128.0f;
        float bt = brightness + t;

        float srs = (sr + saturation);
        float sgs = (sg + saturation);
        float sbs = (sb + saturation);

        float krk = (kr + kelvin);
        float kgk = (kg + kelvin);
        float kbk = (kb + kelvin);

        float[][] saturationMatrix = {{srs, sr, sr},{sg, sgs, sg},{sb, sb, sbs}};
        float[][] kelvinMatrix = {{krk, kr, kr}, {kg, kgk, kg}, {kb, kb, kbk}};

        float[][] rm = new float[3][3];

        for(int i = 0 ; i < 3 ; i ++){
            for (int j = 0 ; j < 3 ; j ++){
                rm[i][j] = 0;
            }
        }
        for(int i = 0 ; i < 3 ; i ++){
            for (int j = 0 ; j < 3 ; j ++){
                for(int z = 0 ; z < 3 ; z ++){
                    rm[i][j] += saturationMatrix[i][z] * kelvinMatrix[z][j];
                }
            }
        }
        for(int i = 0 ; i < 3 ; i ++){
            for (int j = 0 ; j < 3 ; j ++){
                rm[i][j] *= contrast;
            }
        }

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        rm[0][0], rm[0][1], rm[0][2], 0, bt,
                        rm[1][0], rm[1][1], rm[1][2], 0, bt,
                        rm[2][0], rm[2][1], rm[2][2], 0, bt,
                        0, 0, 0, 1, 0
                });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));


        return paint;
    }

}
