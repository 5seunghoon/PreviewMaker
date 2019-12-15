package com.tistory.deque.previewmaker.kotlin.util.extension

import android.R.color
import android.graphics.Color
import kotlin.math.sqrt

fun Int.isBright(): Boolean {
    if (color.transparent == this) return true
    val rgb = intArrayOf(Color.red(this), Color.green(this), Color.blue(this))
    val brightness = sqrt(rgb[0] * rgb[0] * .241 + (rgb[1] * rgb[1] * .691) + rgb[2] * rgb[2] * .068).toInt()
    return brightness >= 200
}