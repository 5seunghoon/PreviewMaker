package com.tistory.deque.previewmaker.Model_Global;

public class PrevClickState implements ClickStateInterface {
    private ClickStateEnum clickStateEnum;
    public PrevClickState(ClickState another){
        clickStateEnum = another.getClickStateEnum();
    }
    @Override
    public ClickStateEnum getClickStateEnum() {
        return clickStateEnum;
    }
}
