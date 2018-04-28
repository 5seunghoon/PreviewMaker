package com.tistory.deque.previewmaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MakeStampActivity extends AppCompatActivity {
  Uri imageURI;
  ImageView imageView;
  EditText editText;
  Button okButton;

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

    okButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clkOkButton(editText.getText().toString());
      }
    });
  }

  private void clkOkButton(String name){
    if(name.length() == 0){

    } else {
      Intent resultIntent = new Intent();
      resultIntent.setData(imageURI);
      resultIntent.putExtra("STAMP_NAME", editText.getText().toString());
      setResult(RESULT_OK);
    }
  }
}
