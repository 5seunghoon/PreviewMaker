package com.tistory.deque.previewmaker.Controler;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.tistory.deque.previewmaker.R;

public class AnimationControler {
    private static Animation aniFadeIn = null;
    private static Animation aniFadeOut = null;
    public static Animation getFadeInAnimation(Context context){
        if(aniFadeIn == null){
            aniFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        }
        return aniFadeIn;
    }
    public static Animation getFadeOutAnimation(Context context){
        if(aniFadeOut == null){
            aniFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        }
        return aniFadeOut;
    }
}
