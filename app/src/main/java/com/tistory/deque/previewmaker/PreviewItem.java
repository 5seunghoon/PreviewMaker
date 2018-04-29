package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

public class PreviewItem {
  private Uri originalImageURI;
  private Uri thumbnailImageURI;

  public PreviewItem(Uri originalImageURI, Uri thumbnailImageURI){
    this.originalImageURI = originalImageURI;
    this.thumbnailImageURI = thumbnailImageURI;
  }

  public void setOriginalImageURI(Uri originalImageURI) {
    this.originalImageURI = originalImageURI;
  }

  public void setThumbnailImageURI(Uri thumbnailImageURI) {
    this.thumbnailImageURI = thumbnailImageURI;
  }

  public Uri getOriginalImageURI() {
    return originalImageURI;
  }

  public Uri getThumbnailImageURI() {
    return thumbnailImageURI;
  }
}
