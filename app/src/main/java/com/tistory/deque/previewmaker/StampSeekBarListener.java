package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

enum STAMP_EDIT_SELECTED {
  BRIGHTNESS,
  CONTRAST
};

public class StampSeekBarListener implements OnSeekBarChangeListener {

  private PreviewEditActivity mActivity;
  private STAMP_EDIT_SELECTED mSelected;
  private PreviewCanvasView mCanvasView;

  public StampSeekBarListener(PreviewEditActivity mActivity, STAMP_EDIT_SELECTED selected, PreviewCanvasView canvasView){
    this.mActivity = mActivity;
    this.mSelected = selected;
    this.mCanvasView = canvasView;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if(mSelected == STAMP_EDIT_SELECTED.BRIGHTNESS) {
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
