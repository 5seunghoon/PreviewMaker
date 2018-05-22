package com.tistory.deque.previewmaker.StampData;

import android.net.Uri;

import com.tistory.deque.previewmaker.Contoler.DBOpenHelper;
import com.tistory.deque.previewmaker.PreviewEditActivity;

public class StampItem {
  private int ID;
  private Uri imageURI;
  private String stampName;
  private int width;
  private int height;
  private int pos_width_per;
  private int pos_height_per;
  private int brightness;
  private StampAnchorEnum pos_anchor;

  public static StampAnchorEnum intToStampAnchor(int value){
    switch (value){
      case 0:
        return StampAnchorEnum.LEFT_TOP;
      case 1:
        return StampAnchorEnum.TOP;
      case 2:
        return StampAnchorEnum.RIGHT_TOP;
      case 3:
        return StampAnchorEnum.LEFT_CENTER;
      case 4:
        return StampAnchorEnum.CENTER;
      case 5:
        return StampAnchorEnum.RIGHT_CENTER;
      case 6:
        return StampAnchorEnum.LEFT_BOTTOM;
      case 7:
        return StampAnchorEnum.BOTTOM;
      case 8:
        return StampAnchorEnum.RIGHT_BOTTOM;
      default:
        return StampAnchorEnum.CENTER;
    }
  }

  public static int stampAnchorToInt(StampAnchorEnum stampAnchorEnum){
    switch (stampAnchorEnum) {
      case LEFT_TOP:
        return 0;
      case TOP:
        return 1;
      case RIGHT_TOP:
        return 2;
      case LEFT_CENTER:
        return 3;
      case CENTER:
        return 4;
      case RIGHT_CENTER:
        return 5;
      case LEFT_BOTTOM:
        return 6;
      case BOTTOM:
        return 7;
      case RIGHT_BOTTOM:
        return 8;
      default:
        return 4;
    }
  }

  public StampItem(int ID, Uri imageURI, String stampName, int width, int height, int pos_width_per, int pos_height_per, int anchorInt) {
    this.ID = ID;
    this.imageURI = imageURI;
    this.stampName = stampName;
    this.width = width;
    this.height = height;
    this.pos_width_per = pos_width_per; // 1 = 0.001%, 100,000 = 100%
    this.pos_height_per = pos_height_per;
    this.brightness = 0;
    this.pos_anchor = intToStampAnchor(anchorInt);
  }

  public StampItem(int ID, Uri imageURI, String stampName){
    this.ID = ID;
    this.imageURI = imageURI;
    this.stampName = stampName;

    this.width = DBOpenHelper.STAMP_WIDTH_KEY_INIT_VALUE;
    this.height = DBOpenHelper.STAMP_HEIGHT_KEY_INIT_VALUE;
    this.pos_width_per = DBOpenHelper.STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE;
    this.pos_height_per = DBOpenHelper.STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE;

    this.brightness = 0;
    this.pos_anchor = StampAnchorEnum.CENTER;
  }

  public int getBrightness() {
    return brightness + PreviewEditActivity.SeekBarBrightnessMax / 2;
  }

  public int getAbsoluteBrightness(){
    return brightness;
  }

  public void setBrightness(int brightness) {
    this.brightness = brightness - PreviewEditActivity.SeekBarBrightnessMax / 2;
  }

  public void resetBrightness(){
    this.brightness = 0;
  }

  public Uri getImageURI() {
    return imageURI;
  }

  public String getStampName() {
    return stampName;
  }

  public int getID() {
    return ID;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getPos_width_per() {
    return pos_width_per;
  }

  public int getPos_height_per() {
    return pos_height_per;
  }

  public void setImageURI(Uri imageURI) {
    this.imageURI = imageURI;
  }

  public void setStampName(String stampName) {
    this.stampName = stampName;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setPos_width_per(int pos_width_per) {
    this.pos_width_per = pos_width_per;
  }

  public void setPos_height_per(int pos_height_per) {
    this.pos_height_per = pos_height_per;
  }

  public StampAnchorEnum getPos_anchor() {
    return pos_anchor;
  }

  public void setPos_anchor(StampAnchorEnum pos_anchor) {
    this.pos_anchor = pos_anchor;
  }
}
