package com.tistory.deque.previewmaker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

public class MakeStampActivity extends AppCompatActivity {
  private final String TAG = "MakeStampActivity";

  Uri imageURI;
  ImageView imageView;
  EditText editText;
  Button okButton;
  LinearLayout stampImageLayout;

  long backPressedTime;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_make_stamp);

    Intent intent = getIntent();
    imageURI = intent.getData();

    imageView = findViewById(R.id.stampImageView);
    editText = findViewById(R.id.stampNameEditText);
    okButton = findViewById(R.id.stampNameOkButton);

    setTitle(R.string.tile_make_stamp_activity);
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
      File file = new File(imageURI.getPath());
      if(file.delete()) {
        Log.d(TAG, "Stamp delete suc");
        /**
         * Do media scan
         */
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageURI);
        sendBroadcast(mediaScanIntent);
        Log.d(TAG, "media scanning end");
      } else {
        Log.d(TAG, "Stamp delete fail" + imageURI);
      }
      Intent resultIntent = new Intent();
      setResult(RESULT_CANCELED, resultIntent);
      finish();
    }
    else {
      Snackbar
        .make(findViewById(R.id.activityMakeStampMainLayout)
          , "낙관 설정을 취소하시려면 뒤로 버튼을 한번 더 눌려주세요."
          , Snackbar.LENGTH_LONG)
        .show();
      backPressedTime = System.currentTimeMillis();
    }
  }

  private void clkOkButton(String name){
    if(name.length() == 0) {
      Snackbar
        .make(findViewById(R.id.activityMakeStampMainLayout)
          , "낙관의 이름을 입력해주세요."
          , Snackbar.LENGTH_LONG)
        .show();
    } else if (name.length() > 10) {
      Snackbar
        .make(findViewById(R.id.activityMakeStampMainLayout)
          , "낙관의 이름을 10자 이내로 줄여주실수 있나요?"
          , Snackbar.LENGTH_LONG)
        .show();
    } else {
      Intent resultIntent = new Intent();
      resultIntent.setData(imageURI);
      resultIntent.putExtra("STAMP_NAME", editText.getText().toString());
      setResult(RESULT_OK, resultIntent);
      finish();
    }
  }
}
