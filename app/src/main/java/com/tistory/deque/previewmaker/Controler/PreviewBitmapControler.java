package com.tistory.deque.previewmaker.Controler;

import android.app.Activity;
import android.graphics.Bitmap;

import com.tistory.deque.previewmaker.Model_PreviewData.PreviewItem;

public class PreviewBitmapControler {
    private static PreviewBitmapControler pbc;
    private Bitmap previewBitmap;
    private Activity mActivity;

    private PreviewBitmapControler(Activity mActivity){
        this.mActivity = mActivity;
    }

    public static PreviewBitmapControler getPreviewBitmapControler(Activity mActivity){
        //싱글턴 패턴
        if(pbc == null){
            pbc = new PreviewBitmapControler(mActivity);
        }
        return pbc;
    }

    public Bitmap getPreviewBitmap() {
        return previewBitmap;
    }

    public void setPreviewBitmap(PreviewItem previewItem){
        this.previewBitmap = null;
        this.previewBitmap = previewItem.getBitmap();
    }
}
