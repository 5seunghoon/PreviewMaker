package com.tistory.deque.previewmaker.Util;

import android.content.Context;
import android.util.TypedValue;

public class CalcUtil {
    public static int convertDpToPx(Context context, int DP){
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP, context.getResources().getDisplayMetrics());
        return px;
    }
}
