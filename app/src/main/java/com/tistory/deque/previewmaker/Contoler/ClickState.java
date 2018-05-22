package com.tistory.deque.previewmaker.Contoler;

public class ClickState {
  /**
   * STATE PATTERN
   */
  private ClickStateEnum clickStateEnum;
  private static ClickState clickState;
  private ClickState(){
    clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
  }

  public static ClickState getClickState() {
    if(clickState == null){
      clickState = new ClickState();
    }
    return clickState;
  }

  public ClickStateEnum getClickStateEnum() {
    return clickStateEnum;
  }

  public void start(){
    clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
  }
  public void clickStampButton(){
    clickStateEnum = ClickStateEnum.STATE_STAMP_EDIT;
  }
  public boolean isShowGuideLine() {
    return clickStateEnum != ClickStateEnum.STATE_NONE_CLICK;
  }

  public void clickFinishStampEdit(){
    switch ((clickStateEnum)) {
      case STATE_NONE_CLICK:
        break;
      case STATE_STAMP_EDIT:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
      case STATE_STAMP_ZOOM:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
    }
  }

  public void clickStamp(){
    switch (clickStateEnum){
      case STATE_NONE_CLICK:
        clickStateEnum = ClickStateEnum.STATE_STAMP_EDIT;
        break;
      case STATE_STAMP_EDIT:
        break;
      case STATE_STAMP_ZOOM:
        break;
    }
  }

  public void clickStampZoomStart(){
    switch (clickStateEnum) {
      case STATE_NONE_CLICK:
        break;
      case STATE_STAMP_EDIT:
        clickStateEnum = ClickStateEnum.STATE_STAMP_ZOOM;
        break;
      case STATE_STAMP_ZOOM:
        break;
    }
  }

  public void clickStampZoomEnd(){
    switch (clickStateEnum) {
      case STATE_NONE_CLICK:
        break;
      case STATE_STAMP_EDIT:
        break;
      case STATE_STAMP_ZOOM:
        clickStateEnum = ClickStateEnum.STATE_STAMP_EDIT;
        break;
    }
  }

  public void clickSave(){
    switch (clickStateEnum) {
      case STATE_NONE_CLICK:
        break;
      case STATE_STAMP_EDIT:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
      case STATE_STAMP_ZOOM:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
    }
  }

  public void finish(){
    clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
  }

}
