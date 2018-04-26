package com.tistory.deque.previewmaker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Permission {
  static private final String TAG = "Permission";

  private String[] permissions = {
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private final int MULTIPLE_PERMISSIONS = 200; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
  private Snackbar permissionSnackbar, permissionSnackbarRational;

  private Activity activity;
  private Context context;

  public Permission(Context context, Activity activity){
    this.activity = activity;
    this.context = context;
  }

  public void requestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MULTIPLE_PERMISSIONS: {
        if (grantResults.length > 0) {
          for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(this.permissions[0])) {
              if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showNoPermissionToastAndFinish();
              }
            } else if (permissions[i].equals(this.permissions[1])) {
              if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showNoPermissionToastAndFinish();
              }
            }
          }
        }
        else{
          showNoPermissionToastAndFinish();
        }
        return;
      }
    }
  }

  public void showNoPermissionToastAndFinish() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity ,Manifest.permission.READ_EXTERNAL_STORAGE)) {
      permissionSnackbar.show();
    } else {
      permissionSnackbarRational.show();
    }
  }

  public boolean checkPermissions() {
    Log.d(TAG, "check permissions func in");
    int result;
    List<String> permissionList = new ArrayList<>();
    for (String pm : permissions) {
      result = ContextCompat.checkSelfPermission(context, pm);
      if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
        permissionList.add(pm);
      }
    }
    if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
      ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
      return false;
    }

    return true;
  }

  public void permissionSnackbarInit(View v){
    permissionSnackbar = Snackbar.make(v, "권한 요청에 동의 해주셔야 해요. 오른쪽 \'GO!\' 클릭!", Snackbar.LENGTH_LONG);
    permissionSnackbar.setAction("GO!", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        checkPermissions();
      }
    });

    permissionSnackbarRational = Snackbar.make(v, "권한이 필요해요 ㅜㅜ\n[설정]-[애플리케이션] 에서 권한을 허용해주세요", Snackbar.LENGTH_LONG);
  }
}
