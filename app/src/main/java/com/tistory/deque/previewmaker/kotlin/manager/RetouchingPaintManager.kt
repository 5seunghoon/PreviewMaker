package com.tistory.deque.previewmaker.kotlin.manager

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object RetouchingPaintManager {

    fun getPaint(contrast: Float, brightness: Float, saturation: Float, kelvin: Float): Paint {
        //contrast : 1, brightness : 0, saturation : 1 is init value
        //https://docs.rainmeter.net/tips/colormatrix-guide/
        val sr = (1 - saturation) * 0.2125f
        val sg = (1 - saturation) * 0.7154f
        val sb = (1 - saturation) * 0.0721f

        val kr = (1 - kelvin) * 0.07f
        val kg = (1 - kelvin) * 0.33f
        val kb = (1 - kelvin) * 0.6f

        val t = (1.0f - contrast) * 128.0f
        val bt = brightness + t

        val srs = sr + saturation
        val sgs = sg + saturation
        val sbs = sb + saturation

        val krk = kr + kelvin
        val kgk = kg + kelvin
        val kbk = kb + kelvin

        val saturationMatrix = arrayOf(floatArrayOf(srs, sr, sr), floatArrayOf(sg, sgs, sg), floatArrayOf(sb, sb, sbs))
        val kelvinMatrix = arrayOf(floatArrayOf(krk, kr, kr), floatArrayOf(kg, kgk, kg), floatArrayOf(kb, kb, kbk))

        val rm = Array(3) { FloatArray(3) }

        for (i in 0..2) {
            for (j in 0..2) {
                rm[i][j] = 0f
            }
        }
        for (i in 0..2) {
            for (j in 0..2) {
                for (z in 0..2) {
                    rm[i][j] += saturationMatrix[i][z] * kelvinMatrix[z][j]
                }
            }
        }
        for (i in 0..2) {
            for (j in 0..2) {
                rm[i][j] *= contrast
            }
        }

        val cm = ColorMatrix(floatArrayOf(
                rm[0][0], rm[0][1], rm[0][2], 0f, bt,
                rm[1][0], rm[1][1], rm[1][2], 0f, bt,
                rm[2][0], rm[2][1], rm[2][2], 0f, bt,
                0f, 0f, 0f, 1f, 0f
        ))

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)

        return paint
    }

}