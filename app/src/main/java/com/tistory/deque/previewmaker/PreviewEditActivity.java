package com.tistory.deque.previewmaker;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PreviewEditActivity extends AppCompatActivity {
  private final String TAG = "PreviewEditActivity";
  int stampID;
  Uri stampImageURI;

  StampItem stamp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preview_edit);
    Intent intent = getIntent();
    stampID = intent.getExtras().getInt("STAMP_ID");
    stampImageURI = intent.getData();

    setTitle(R.string.title_preview_make_activity);
  }
}
