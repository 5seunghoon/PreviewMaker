package com.tistory.deque.previewmaker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
  implements NavigationView.OnNavigationItemSelectedListener {
  private final int REQUEST_TAKE_STAMP_FROM_ALBUM = 101;
  private final int REQUEST_IMAGE_CROP = 102;
  private final String TAG = "MainActivity";

  private Toolbar toolbar;
  private Permission permission;

  String mCurrentPhotoPath;
  Uri imageURI, cropSourceURI, cropEndURI; //  cropSourceURI = 자를 uri, cropEndURI = 자르고 난뒤 uri

  ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    permission = new Permission(getApplicationContext(), this);
    permission.permissionSnackbarInit(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(!permission.checkPermissions()) return;
        getStampFromAlbum();
      }
    });

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    permission.checkPermissions();

    imageView = findViewById(R.id.tempImageView);
  }

  private void getStampFromAlbum() {
    Log.d(TAG, "getAlbum()");
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
    Log.d(TAG, "start Activity : album intent");
    startActivityForResult(intent, REQUEST_TAKE_STAMP_FROM_ALBUM);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode){
      case REQUEST_TAKE_STAMP_FROM_ALBUM:
        if(resultCode == Activity.RESULT_OK){
          File albumFile = null;
          try{
            albumFile = createImageFile();
          } catch (IOException e){
            Log.d(TAG, "Create dump image file IO Exception");
          }
          cropSourceURI = data.getData();
          cropEndURI = Uri.fromFile(albumFile);
          cropImage();
          Log.d(TAG, "TAKE STAMP FROM ALBUM OK");
        } else {
          Log.d(TAG, "TAKE STAMP FROM ALBUM FAIL");
        }
        break;

      case REQUEST_IMAGE_CROP:
        if(resultCode == Activity.RESULT_OK){
          galleryAddPic();
          imageView.setImageURI(cropEndURI);
          Log.d(TAG, "IMAGE CROP OK");
        } else {
          Log.d(TAG, "IMAGE CROP FAIL");
        }
        break;
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_camera) {
      // Handle the camera action
    } else if (id == R.id.nav_gallery) {

    } else if (id == R.id.nav_slideshow) {

    } else if (id == R.id.nav_manage) {

    } else if (id == R.id.nav_share) {

    } else if (id == R.id.nav_send) {

    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permission.requestPermissionsResult(requestCode, permissions, grantResults);
  }

  public File createImageFile() throws IOException {
    Log.d(TAG, "createImageFile func");
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + ".jpg";
    File imageFile = null;
    File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "Preview Maker");
    Log.d(TAG, "storageDir : " + storageDir);
    if (!storageDir.exists()) {
      Log.d(TAG, storageDir.toString() + " is not exist");
      storageDir.mkdir();
      Log.d(TAG, "storageDir make");
    }
    imageFile = new File(storageDir, imageFileName);
    Log.d(TAG, "imageFile init");
    mCurrentPhotoPath = imageFile.getAbsolutePath();
    Log.d(TAG, "mCurrentPhotoPath : " + mCurrentPhotoPath);

    return imageFile;
  }

  public void cropImage() {
    /**
     * cropSourceURI = 자를 uri
     * cropEndURI = 자르고 난뒤 uri
     */
    Log.d(TAG, "cropImage() CALL");
    Log.d(TAG, "cropImage() : Photo URI, Album URI" + cropSourceURI + ", " + cropEndURI);

    Intent cropIntent = new Intent("com.android.camera.action.CROP");

    cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    cropIntent.setDataAndType(cropSourceURI, "image/*");
    cropIntent.putExtra("output", cropEndURI);
    startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
  }
  private void galleryAddPic() {
    /**
     * Do media scan
     */
    Log.d(TAG, "galleryAddPic, do media scan");
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(mCurrentPhotoPath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    sendBroadcast(mediaScanIntent);
    Log.d(TAG, "media scanning end");
  }


}
