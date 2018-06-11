package com.tistory.deque.previewmaker.Model_Global;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.tistory.deque.previewmaker.Controler.PreviewCanvasView;
import com.tistory.deque.previewmaker.Controler.PreviewEditActivity;

;

public class StampSeekBarListener implements OnSeekBarChangeListener {

  private PreviewEditActivity mActivity;
  private StampEditSelectedEnum mSelected;
  private PreviewCanvasView mCanvasView;

  public StampSeekBarListener(PreviewEditActivity mActivity, StampEditSelectedEnum selected, PreviewCanvasView canvasView){
    this.mActivity = mActivity;
    this.mSelected = selected;
    this.mCanvasView = canvasView;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if(mSelected == StampEditSelectedEnum.BRIGHTNESS) {
      mCanvasView.onDrawStampBrigtness(progress);
      mActivity.setStampSeekBarText(progress, mSelected);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {

  }
}
