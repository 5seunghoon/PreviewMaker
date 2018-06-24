package com.tistory.deque.previewmaker.Model_Global;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tistory.deque.previewmaker.Activity.PreviewCanvasView;
import com.tistory.deque.previewmaker.Activity.PreviewEditActivity;

;

public class StampSeekBarListener implements OnSeekBarChangeListener {

    private PreviewEditActivity mActivity;
    private StampEditSelectedEnum mSelected;
    private PreviewCanvasView mCanvasView;

    public StampSeekBarListener(PreviewEditActivity mActivity, StampEditSelectedEnum selected, PreviewCanvasView canvasView) {
        this.mActivity = mActivity;
        this.mSelected = selected;
        this.mCanvasView = canvasView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //시크바가 바뀔때마다 콜백
        if (mSelected == StampEditSelectedEnum.BRIGHTNESS) {
            mCanvasView.onDrawStampBrigtness(progress); // 스탬프의 밝기를 바꿈
            mActivity.setStampSeekBarText(progress, mSelected); // 시크바 옆의 텍스트를 바꿈
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
