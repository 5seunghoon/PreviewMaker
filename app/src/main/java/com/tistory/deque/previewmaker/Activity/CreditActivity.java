package com.tistory.deque.previewmaker.Activity;

import android.content.pm.PackageInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.tistory.deque.previewmaker.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreditActivity extends AppCompatActivity {
    @BindView(R.id.creditActivityAppNameTextView)
    TextView creditActivityAppNameTextView;
    @BindView(R.id.creditActivityVersionTextView)
    TextView creditActivityVersionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        setTitle(getString(R.string.title_credit_activity));

        ButterKnife.bind(this);

        //뒤로가기 버튼
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        String appVersion;
        try {
            PackageInfo i = getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            appVersion = i.versionName;
        } catch (Exception e) {
            appVersion = "1.0.0";
        }
        creditActivityVersionTextView.setText(appVersion);
        creditActivityAppNameTextView.setText(getString(R.string.app_name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //뒤로가기 버튼
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick(R.id.creditActivityOpenSourceTextView)
    public void opensource(){

    }
}