package com.tistory.deque.previewmaker;

import android.net.Uri;

public class StampItem {
  Uri imageURI;
  String stampName;

  public StampItem(Uri imageURI, String stampName) {
    this.imageURI = imageURI;
    this.stampName = stampName;
  }

  public Uri getImageURI() {
    return imageURI;
  }
  public String getStampName() {
    return stampName;
  }

  public void setImageURI(Uri imageURI) {
    this.imageURI = imageURI;
  }

  public void setStampName(String stampName) {
    this.stampName = stampName;
  }
}
