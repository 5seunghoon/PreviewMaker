package com.tistory.deque.previewmaker.Model_Global;

public class ClickState implements ClickStateInterface{
    /**
     * STATE PATTERN
     */
    private ClickStateEnum prevStateEnum;
    private ClickStateEnum clickStateEnum;
    private static ClickState clickState;

    private ClickState() {
        prevStateEnum = ClickStateEnum.STATE_NONE_CLICK;
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
    }

    public static ClickState getClickState() {
        if (clickState == null) {
            clickState = new ClickState();
        }
        return clickState;
    }

    @Override
    public ClickStateEnum getClickStateEnum() {
        return clickStateEnum;
    }

    @Override
    public ClickStateEnum getPrevClickStateEnum(){
        return prevStateEnum;
    }


    public void clickFilterButton(){
        prevStateEnum = clickStateEnum;
        clickStateEnum = ClickStateEnum.STATE_BITMAP_FILTER;
    }

    public void clickFinishBlur() {
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                clickStateEnum = ClickStateEnum.STATE_BITMAP_FILTER;
                break;
            case STATE_BITMAP_FILTER_BLUR:
                clickStateEnum = ClickStateEnum.STATE_BITMAP_FILTER;
                break;
        }
    }

    public void endBlurGuide(){
        /**
         * BLUR_GUIDE -> BLUR
         */
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                clickStateEnum = ClickStateEnum.STATE_BITMAP_FILTER_BLUR;
                break;
            case STATE_BITMAP_FILTER_BLUR:
                break;
        }
    }

    public void restartBlurGuide(){
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
            case STATE_BITMAP_FILTER_BLUR:
                clickStateEnum = ClickStateEnum.STATE_BITMAP_FILTER_BLUR_GUIDE;
                break;
        }
    }

    public void start() {
        prevStateEnum = clickStateEnum;
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
    }

    public void clickStampButton() {
        prevStateEnum = clickStateEnum;
        clickStateEnum = ClickStateEnum.STATE_STAMP_EDIT;
    }

    public void clickBlurButton(){
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum){
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                clickStateEnum = ClickStateEnum.STATE_BITMAP_FILTER_BLUR_GUIDE;
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
            case STATE_BITMAP_FILTER_BLUR:
                break;
        }
    }


    public void clickFinishFilterEdit(){
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
        }
    }

    public void clickFinishStampEdit() {
        prevStateEnum = clickStateEnum;
        switch ((clickStateEnum)) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
                break;
            case STATE_STAMP_ZOOM:
                clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
        }
    }

    public void clickStamp() {
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                clickStateEnum = ClickStateEnum.STATE_STAMP_EDIT;
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
        }
    }

    public void clickStampZoomStart() {
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                clickStateEnum = ClickStateEnum.STATE_STAMP_ZOOM;
                break;
            case STATE_STAMP_ZOOM:
                break;
            case STATE_BITMAP_FILTER:
                break;
        }
    }

    public void clickStampZoomEnd() {
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                break;
            case STATE_STAMP_ZOOM:
                clickStateEnum = ClickStateEnum.STATE_STAMP_EDIT;
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
        }
    }

    public void clickSave() {
        prevStateEnum = clickStateEnum;
        switch (clickStateEnum) {
            case STATE_NONE_CLICK:
                break;
            case STATE_STAMP_EDIT:
                clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
                break;
            case STATE_STAMP_ZOOM:
                clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
                break;
            case STATE_BITMAP_FILTER:
                break;
            case STATE_BITMAP_FILTER_BLUR_GUIDE:
                break;
        }
    }

    public void finish() {
        prevStateEnum = clickStateEnum;
        clickStateEnum = ClickStateEnum.STATE_NONE_CLICK;
    }

    public boolean isShowGuideLine() {
        return (clickStateEnum == ClickStateEnum.STATE_STAMP_EDIT ||
                clickStateEnum == ClickStateEnum.STATE_STAMP_ZOOM);
    }

    public boolean isBlur(){
        return (clickStateEnum == ClickStateEnum.STATE_BITMAP_FILTER_BLUR);
    }

    public boolean isBlurGuide(){
        return (clickStateEnum == ClickStateEnum.STATE_BITMAP_FILTER_BLUR_GUIDE);
    }


}
