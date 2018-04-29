package com.tistory.deque.previewmaker;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class PreviewEditActivity extends AppCompatActivity {
  private final String TAG = "PreviewEditActivity";
  public static final String EXTRA_STAMP_ID = "STAMP_ID";
  public static final String EXTRA_PREVIEW_LIST = "PREVIEW_LIST";

  int stampID;
  Uri stampImageURI;

  ArrayList<String> previewPaths;

  StampItem stamp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preview_edit);
    Intent intent = getIntent();
    stampID = intent.getExtras().getInt("STAMP_ID");
    stampImageURI = intent.getData();
    previewPaths = new ArrayList<>();
    previewPaths = intent.getStringArrayListExtra(EXTRA_PREVIEW_LIST);

    for (String previewPath : previewPaths) {
      Logger.d(TAG, previewPath);
    }

    setTitle(R.string.title_preview_make_activity);
  }
}
