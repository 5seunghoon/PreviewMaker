package com.tistory.deque.previewmaker;

import android.net.Uri;

public class StampItem {
  int ID;
  Uri imageURI;
  String stampName;
  private int width;
  private int height;
  private int pos_width_per;
  private int pos_height_per;

  public StampItem(int ID, Uri imageURI, String stampName, int width, int height, int pos_width_per, int pos_height_per) {
    this.ID = ID;
    this.imageURI = imageURI;
    this.stampName = stampName;
    this.width = width;
    this.height = height;
    this.pos_width_per = pos_width_per; // 1 = 0.001%, 100,000 = 100%
    this.pos_height_per = pos_height_per;
  }

  public StampItem(int ID, Uri imageURI, String stampName){
    this.ID = ID;
    this.imageURI = imageURI;
    this.stampName = stampName;

    this.width = DBOpenHelper.STAMP_WIDTH_KEY_INIT_VALUE;
    this.height = DBOpenHelper.STAMP_HEIGHT_KEY_INIT_VALUE;
    this.pos_width_per = DBOpenHelper.STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE;
    this.pos_height_per = DBOpenHelper.STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE;
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
}
