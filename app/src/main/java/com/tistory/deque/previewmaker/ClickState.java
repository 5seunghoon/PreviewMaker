package com.tistory.deque.previewmaker;

enum ClickStateEnum {
  STATE_NONE_CLICK,
  STATE_STAMP_CLICK,
  STATE_STAMP_CLICK_EDIT,
  STATE_PREVIEW_CLICK,
  STATE_PREVIEW_CLICK_EDIT
}

class ClickState {
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
  public void clickFinishStampEdit(){
    switch ((clickStateEnum)) {
      case STATE_NONE_CLICK:
        break;
      case STATE_STAMP_CLICK:
        break;
      case STATE_STAMP_CLICK_EDIT:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
      case STATE_PREVIEW_CLICK:
        break;
      case STATE_PREVIEW_CLICK_EDIT:
        break;
    }
  }

  public void clickStamp(){
    switch (clickStateEnum){
      case STATE_NONE_CLICK:
        clickStateEnum = ClickStateEnum.STATE_STAMP_CLICK;
        break;
      case STATE_STAMP_CLICK:
        clickStateEnum = ClickStateEnum.STATE_STAMP_CLICK_EDIT;
        break;
      case STATE_STAMP_CLICK_EDIT:
        break;
      case STATE_PREVIEW_CLICK:
        clickStateEnum = ClickStateEnum.STATE_STAMP_CLICK;
        break;
      case STATE_PREVIEW_CLICK_EDIT:
        break;
    }
  }

  public void clickPreview(){
    switch (clickStateEnum) {
      case STATE_NONE_CLICK:
        clickStateEnum = ClickStateEnum.STATE_PREVIEW_CLICK;
        break;
      case STATE_STAMP_CLICK:
        clickStateEnum = ClickStateEnum.STATE_PREVIEW_CLICK;
        break;
      case STATE_STAMP_CLICK_EDIT:
        break;
      case STATE_PREVIEW_CLICK:
        clickStateEnum = ClickStateEnum.STATE_PREVIEW_CLICK_EDIT;
        break;
      case STATE_PREVIEW_CLICK_EDIT:
        break;
    }
  }

  public void clickSave(){
    switch (clickStateEnum) {
      case STATE_NONE_CLICK:
        break;
      case STATE_STAMP_CLICK:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
      case STATE_STAMP_CLICK_EDIT:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
      case STATE_PREVIEW_CLICK:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
      case STATE_PREVIEW_CLICK_EDIT:
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        break;
    }
  }

}
