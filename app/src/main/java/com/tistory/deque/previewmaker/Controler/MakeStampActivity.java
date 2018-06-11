package com.tistory.deque.previewmaker.Controler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.tistory.deque.previewmaker.R;
import com.tistory.deque.previewmaker.Util.Logger;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MakeStampActivity extends AppCompatActivity {
  public static final String STAMP_NAME = "STAMP_NAME";
  private final String TAG = "MakeStampActivity";

  Uri imageURI;

  @BindView(R.id.stampImageView)
  ImageView stampImageView;
  @BindView(R.id.stampNameEditText)
  EditText stampNameEditText;

  private long backPressedTime;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_make_stamp);

    ButterKnife.bind(this);

    Intent intent = getIntent();
    imageURI = intent.getData();

    setTitle(R.string.title_make_stamp_activity);
    stampImageView.setImageURI(imageURI);

  }

  @Override
  public void onBackPressed() {
    if(System.currentTimeMillis() - backPressedTime < 2000){
      deleteFile(imageURI);
      Intent resultIntent = new Intent();
      setResult(RESULT_CANCELED, resultIntent);
      finish();
    }
    else {
      Snackbar
        .make(findViewById(R.id.activityMakeStampMainLayout)
          , getString(R.string.snackbar_make_stamp_acti_back_to_exit)
          , Snackbar.LENGTH_LONG)
        .show();
      backPressedTime = System.currentTimeMillis();
    }
  }

  private void deleteFile(Uri uri){
    File file = new File(uri.getPath());
    if(file.delete()) {
      MainActivity.galleryAddPic(this, uri);
    } else {
      Logger.d(TAG, "Stamp delete fail" + uri);
    }
  }

  @OnClick(R.id.stampNameOkButton)
  public void clickOkButton(){
    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(stampNameEditText.getWindowToken(), 0);
    checkName(stampNameEditText.getText().toString());
  }

  private void checkName(String name){
    if(name.length() == 0) {
      Snackbar
        .make(findViewById(R.id.activityMakeStampMainLayout)
          , getString(R.string.snackbar_make_stamp_acti_no_name_warn)
          , Snackbar.LENGTH_LONG)
        .show();
    } else if (name.length() > 10) {
      Snackbar
        .make(findViewById(R.id.activityMakeStampMainLayout)
          , getString(R.string.snackbar_make_stamp_acti_name_len_warn)
          , Snackbar.LENGTH_LONG)
        .show();
    } else {
      Intent resultIntent = new Intent();
      resultIntent.setData(imageURI);
      resultIntent.putExtra(STAMP_NAME, stampNameEditText.getText().toString());
      setResult(RESULT_OK, resultIntent);
      finish();
    }
  }
}
