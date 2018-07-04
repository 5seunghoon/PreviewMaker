package com.tistory.deque.previewmaker.Model_Global;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tistory.deque.previewmaker.Activity.PreviewCanvasView;
import com.tistory.deque.previewmaker.Activity.PreviewEditActivity;

;

public class SeekBarListener implements OnSeekBarChangeListener {

    public static int SeekBarStampBrightnessMax = 512;
    public static int SeekBarPreviewBrightnessMax = 512;
    public static int SeekBarPreviewContrastMax = 512;
    public static int SeekBarPreviewSaturationMax = 512;
    public static int SeekBarPreviewKelvinMax = 512;

    private PreviewEditActivity mActivity;
    private SeekBarSelectedEnum mSelected;
    private PreviewCanvasView mCanvasView;


    public SeekBarListener(PreviewEditActivity mActivity, SeekBarSelectedEnum selected, PreviewCanvasView canvasView) {
        this.mActivity = mActivity;
        this.mSelected = selected;
        this.mCanvasView = canvasView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //시크바의 값이 바뀔때마다 콜백
        switch (mSelected){
            case BRIGHTNESS:
                mCanvasView.onDrawStampBrigtness(progress); // 스탬프의 밝기를 바꿈
                mActivity.setStampSeekBarText(progress, mSelected); // 시크바 옆의 텍스트를 바꿈
                break;
            case CONTRAST:
                break;
            case PREVIEW_BRIGHTNESS:
            case PREVIEW_CONTRAST:
            case PREVIEW_SATURATION:
            case PREVIEW_KELVIN:
                mCanvasView.onDrawPreviewColorParam(progress, mSelected);
                mActivity.setStampSeekBarText(progress, mSelected);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}