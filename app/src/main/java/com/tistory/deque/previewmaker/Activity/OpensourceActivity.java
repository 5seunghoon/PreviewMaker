package com.tistory.deque.previewmaker.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tistory.deque.previewmaker.R;

public class OpensourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opensource);
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}
