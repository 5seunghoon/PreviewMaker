package com.tistory.deque.previewmaker.kotlin.util.extension

import android.view.View
import android.view.animation.AlphaAnimation

private const val animationDuration = 300L

fun View.fadeOut() {
    val fadeOutAnimation = AlphaAnimation(1f, 0f).apply { duration = animationDuration }
    this.run {
        post {
            visibility = View.GONE
            animation = fadeOutAnimation
        }
    }
}

fun View.fadeIn() {
    val fadeInAnimation = AlphaAnimation(0f, 1f).apply { duration = animationDuration }
    this.run {
        post {
            visibility = View.VISIBLE
            animation = fadeInAnimation
        }
    }
}

fun View.goneView(){
    this.run { post { visibility = View.GONE } }
}

fun View.invisibleView(){
    this.run { post { visibility = View.GONE } }
}

fun View.visibleView(){
    this.run { post { visibility = View.VISIBLE } }
}