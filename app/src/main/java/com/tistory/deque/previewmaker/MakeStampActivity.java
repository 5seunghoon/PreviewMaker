package com.tistory.deque.previewmaker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class MakeStampActivity extends AppCompatActivity {
  public static final String STAMP_NAME = "STAMP_NAME";
  private final String TAG = "MakeStampActivity";

  private Uri imageURI;
  private ImageView imageView;
  private EditText editText;
  private Button okButton;

  private long backPressedTime;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_make_stamp);

    Intent intent = getIntent();
    imageURI = intent.getData();

    imageView = findViewById(R.id.stampImageView);
    editText = findViewById(R.id.stampNameEditText);
    okButton = findViewById(R.id.stampNameOkButton);

    setTitle(R.string.title_make_stamp_activity);
    imageView.setImageURI(imageURI);

    okButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        clkOkButton(editText.getText().toString());
      }
    });


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

  private void clkOkButton(String name){
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
      resultIntent.putExtra(STAMP_NAME, editText.getText().toString());
      setResult(RESULT_OK, resultIntent);
      finish();
    }
  }
}
