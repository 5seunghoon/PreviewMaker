package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity
  implements NavigationView.OnNavigationItemSelectedListener {
  public final static String PREVIEW_SAVED_DIRECTORY = "Preview Maker";
  public final static String STAMP_SAVED_DIRECTORY = "Stamp";

  private final int REQUEST_TAKE_STAMP_FROM_ALBUM = 101;
  private final int REQUEST_IMAGE_CROP = 102;
  private final int REQUEST_MAKE_STAMP_ACTIVITY = 103;
  private final int REQUEST_TAKE_PREVIEW_FROM_ALBUM = 104;
  private final String TAG = "MainActivity";

  DBOpenHelper dbOpenHelper;

  Toolbar mToolbar;
  Permission mPermission;
  TextView mMainActivityHintTextView;

  String mCurrentPhotoPath;
  Uri mCropSourceURI, mCropEndURI; //  mCropSourceURI = 자를 uri, mCropEndURI = 자르고 난뒤 uri

  RecyclerView mRecyclerStampView;
  ArrayList<StampItem> mStampItems;
  StampAdapter mStampAdapter;
  LinearLayoutManager mRecyclerViewLayoutManager;

  ArrayList<String> mSeletedPreviews;



  long mBackPressedTime;
  int position;


  @Override
  protected void onDestroy() {
    if (dbOpenHelper != null) {
      dbOpenHelper.dbClose();
    }
    super.onDestroy();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    dbOpen();

    mMainActivityHintTextView = findViewById(R.id.mainActivityHintText);

    //setting toolbar
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);

    //permission
    mPermission = new Permission(getApplicationContext(), this);
    mPermission.permissionSnackbarInit(mToolbar);

    //setting recycler view
    setRecyclerView();

    //floating action button
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(!mPermission.checkPermissions()) return;
        getStampFromAlbum();
      }
    });

    setTitle("프리뷰 메이커");

    //setting drawer
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    //setting navigation view
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);


    mPermission.checkPermissions();

    stampsFromDBToList();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode){
      case REQUEST_TAKE_STAMP_FROM_ALBUM:
        if(resultCode == Activity.RESULT_OK){
          File albumFile = null;
          albumFile = createImageFile();
          mCropSourceURI = data.getData();
          Logger.d(TAG, "mCropSourceURI : " + mCropSourceURI);
          mCropEndURI = Uri.fromFile(albumFile);

          //cropImage();
          nonCropImage();

          Logger.d(TAG, "TAKE STAMP FROM ALBUM OK");
        } else {
          Logger.d(TAG, "TAKE STAMP FROM ALBUM FAIL");
        }
        break;

      case REQUEST_IMAGE_CROP:
        if(resultCode == Activity.RESULT_OK){
          galleryAddPic();
          Intent intent = new Intent(getApplicationContext(), MakeStampActivity.class);
          intent.setData(mCropEndURI);
          startActivityForResult(intent, REQUEST_MAKE_STAMP_ACTIVITY);
          Logger.d(TAG, "IMAGE CROP OK");
        } else if(resultCode == Activity.RESULT_CANCELED){
          Logger.d(TAG, "IMAGE CROP CANCLE");
        } else {
          Logger.d(TAG, "IMAGE CROP FIRST USER");
        }
        break;

      case REQUEST_MAKE_STAMP_ACTIVITY:
        if(resultCode == Activity.RESULT_OK){
          addStampToListAndDB(requestCode, resultCode, data);
        }
        break;

      case REQUEST_TAKE_PREVIEW_FROM_ALBUM:
        if(resultCode == Activity.RESULT_OK){
          List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

          Intent intent = new Intent(getApplicationContext(), PreviewEditActivity.class);
          intent.putStringArrayListExtra(PreviewEditActivity.EXTRA_PREVIEW_LIST, (ArrayList<String>) path);
          intent.setData(mStampItems.get(position).getImageURI());
          intent.putExtra(PreviewEditActivity.EXTRA_STAMP_ID, mStampItems.get(position).getID());
          startActivity(intent);
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
      if(System.currentTimeMillis() - mBackPressedTime > 2000){
        Snackbar.make(mToolbar, "뒤로 버튼을 한번 더 누르시면 종료합니다", Snackbar.LENGTH_LONG)
          .setAction("EXIT", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
          })
          .show();
        mBackPressedTime = System.currentTimeMillis();
      } else {
        finish();
      }
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

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    mPermission.requestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void dbOpen(){
    dbOpenHelper = DBOpenHelper.getDbOpenHelper(
      getApplicationContext()
      , DBOpenHelper.dpOpenHelperName
      , null
      , DBOpenHelper.dbVersion);
    dbOpenHelper.dbOpen();
  }

  private void viewEveryItemInDB() {
    if(!BuildConfig.DEBUG){
      return;
    }
    int _id;
    String _imageURI;
    String _name;
    String sql = "SELECT * FROM " + dbOpenHelper.TABLE_NAME_STAMPS + ";";
    Cursor results = null;
    results = dbOpenHelper.db.rawQuery(sql, null);
    results.moveToFirst();
    while(!results.isAfterLast()) {
      _id = results.getInt(0);
      _name = results.getString(1);
      _imageURI = results.getString(2);
      Logger.d(TAG, "DB ITEM : id : " + _id + " imageURI : " + _imageURI + " name : " + _name);
      results.moveToNext();
    }
  }

  public File createImageFile() {
    Logger.d(TAG, "createImageFile func");
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "STAMP_" + timeStamp + ".png";
    Logger.d(TAG, "image file name : " + imageFileName
    );
    File imageFile = null;
    File storageParentDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", PREVIEW_SAVED_DIRECTORY);
    File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures/" + PREVIEW_SAVED_DIRECTORY, STAMP_SAVED_DIRECTORY);
    Logger.d(TAG, "storageParentDir : " + storageParentDir);
    Logger.d(TAG, "storageDir : " + storageDir);
    if (!storageParentDir.exists()) {
      storageParentDir.mkdir();
      storageDir.mkdir();
    }
    if(!storageDir.exists()){
      storageDir.mkdir();
    }
    imageFile = new File(storageDir, imageFileName);
    mCurrentPhotoPath = imageFile.getAbsolutePath();
    Logger.d(TAG, "mCurrentPhotoPath : " + mCurrentPhotoPath);

    return imageFile;
  }

  @Deprecated
  public void cropImage() {
    /**
     * mCropSourceURI = 자를 uri
     * mCropEndURI = 자르고 난뒤 uri
     */
    Logger.d(TAG, "cropImage() CALL");
    Logger.d(TAG, "cropImage() : Photo URI, Album URI" + mCropSourceURI + ", " + mCropEndURI);

    Intent cropIntent = new Intent("com.android.camera.action.CROP");

    cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    cropIntent.setDataAndType(mCropSourceURI, "image/*");
    cropIntent.putExtra("output", mCropEndURI);
    startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
  }

  public void nonCropImage(){
    String pathCropSourceURI = getRealPathFromURI(mCropSourceURI);
    File file = new File(pathCropSourceURI);
    File outFile = new File(mCropEndURI.getPath());
    Logger.d(TAG, "inFile , outFile " + file + " , " + outFile);

    if (file != null && file.exists()) {

      try {

        FileInputStream fis = new FileInputStream(file);
        FileOutputStream newfos = new FileOutputStream(outFile);
        int readcount = 0;
        byte[] buffer = new byte[1024];

        while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
          newfos.write(buffer, 0, readcount);
        }
        newfos.close();
        fis.close();
      } catch (Exception e) {
        Logger.d(TAG, "FILE COPY FAIL");
        e.printStackTrace();
      }
    } else {
      Logger.d(TAG, "IN FILE NOT EXIST");
    }

    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(mCropEndURI.getPath());
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    sendBroadcast(mediaScanIntent);

    Intent intent = new Intent(getApplicationContext(), MakeStampActivity.class);
    intent.setData(mCropEndURI);
    startActivityForResult(intent, REQUEST_MAKE_STAMP_ACTIVITY);

  }

  public String getRealPathFromURI(Uri contentUri) {

    String[] proj = { MediaStore.Images.Media.DATA };

    Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
    cursor.moveToNext();
    String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
    Uri uri = Uri.fromFile(new File(path));

    Logger.d(TAG, "getRealPathFromURI(), path : " + uri.toString());

    cursor.close();
    return path;
  }

  private void galleryAddPic() {
    /**
     * Do media scan
     */
    Logger.d(TAG, "galleryAddPic, do media scan");
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(mCurrentPhotoPath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    sendBroadcast(mediaScanIntent);
    Logger.d(TAG, "media scanning end");
  }

  private void addStampToListAndDB(int requestCode, int resultCode, Intent data){
    dbOpenHelper.dbInsertStamp(data.getStringExtra("STAMP_NAME"), data.getData());

    int width, height, posWidthPer, posHeightPer;

    final String MY_QUERY = "SELECT MAX(_id) FROM " + dbOpenHelper.TABLE_NAME_STAMPS;
    Cursor cur = dbOpenHelper.db.rawQuery(MY_QUERY, null);
    cur.moveToFirst();
    int maxID = cur.getInt(0);

    mStampItems.add(new StampItem(maxID, data.getData(), data.getStringExtra("STAMP_NAME"), -1, -1, 50, 50));
    Logger.d(TAG, "INSERT : ID : " + maxID + " imageURI : " + data.getData() + " name : " + data.getStringExtra("STAMP_NAME"));

    viewEveryItemInDB();

    mStampAdapter.notifyDataSetChanged();
    invisibleHint();
  }

  protected void callFromListItem(int position){
    getPreviewsFromAlbum();
    this.position = position;
  }

  protected void callFromListItemToDelete(View v, int position){

    int id = mStampItems.get(position).getID();
    Uri imageURI = mStampItems.get(position).getImageURI();
    String name = mStampItems.get(position).getStampName();
    File file = new File(imageURI.getPath());
    if(!file.exists()) {
      mStampAdapter.notifyDataSetChanged();
      dbOpenHelper.dbDeleteStamp(id);
    } else {
      if(file.delete()) {

        Logger.d(TAG, "Stamp delete suc");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageURI);
        sendBroadcast(mediaScanIntent);
        Logger.d(TAG, "media scanning end");

        try{
          mStampItems.remove(position);
        } catch (IndexOutOfBoundsException e){
          Logger.d(TAG, "out ouf bound");
        }

        mStampAdapter.notifyDataSetChanged();

        dbOpenHelper.dbDeleteStamp(id);

        viewEveryItemInDB();

        visibleHint();

        Snackbar.make(v, "낙관 [" + name + "] 삭제 완료", Snackbar.LENGTH_LONG).show();
      } else {
        Logger.d(TAG, "Stamp delete fail : " + imageURI);
      }
    }
  }

  private void getPreviewsFromAlbum(){
    mSeletedPreviews = new ArrayList<>();

    Intent intent = new Intent(getApplicationContext(), MultiImageSelectorActivity.class);
// whether show camera
    intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
// max select image amount
    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 99);
// select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
// default select images (support array list)
    intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSeletedPreviews);
    startActivityForResult(intent, REQUEST_TAKE_PREVIEW_FROM_ALBUM);
  }

  private void stampsFromDBToList() {
    int id;
    int width, height, posWidthPer, posHeightPer;
    String imageURIPath;
    String name;
    String sql = "SELECT * FROM " + dbOpenHelper.TABLE_NAME_STAMPS + ";";
    Cursor results = null;
    results = dbOpenHelper.db.rawQuery(sql, null);
    Logger.d(TAG, "Cursor open");
    results.moveToFirst();
    while(!results.isAfterLast()) {
      id = results.getInt(0);
      name = results.getString(1);
      imageURIPath = results.getString(2);
      width = results.getInt(3);
      height = results.getInt(4);
      posWidthPer = results.getInt(5);
      posHeightPer = results.getInt(6);

      Logger.d(TAG, "DB ITEM : id : " + id + " imageURIPath : " + imageURIPath + " name : " + name);

      //if there is not exist stamp file, delete it in db
      String imageURIFilePath = Uri.parse(imageURIPath).getPath();
      File stampFile = new File(imageURIFilePath);

      Logger.d(TAG, " imageURIPath : " + imageURIPath );
      if (!stampFile.exists()) {
        dbOpenHelper.dbDeleteStamp(id);
      } else {
        mStampItems.add(new StampItem(id, Uri.parse(imageURIPath), name, width, height, posWidthPer, posHeightPer));
      }

      //if(id > endOfID) endOfID = id;

      results.moveToNext();
    }
    invisibleHint();
  }

  protected void invisibleHint(){
    if(mStampItems.size() > 0)  mMainActivityHintTextView.setVisibility(View.GONE);
    Logger.d(TAG, mStampItems.size() +  " ->mStampItems size");
  }

  protected void visibleHint(){
    if(mStampItems.size() <= 0)  mMainActivityHintTextView.setVisibility(View.VISIBLE);
    Logger.d(TAG, mStampItems.size() +  " ->mStampItems size");
  }

  private void setRecyclerView(){
    mRecyclerStampView = findViewById(R.id.recyclerStampView);
    mRecyclerStampView.setHasFixedSize(true);

    mRecyclerViewLayoutManager = new LinearLayoutManager(this);
    mRecyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerStampView.setLayoutManager(mRecyclerViewLayoutManager);
    mRecyclerStampView.setItemAnimator(new DefaultItemAnimator());

    mStampItems = new ArrayList<>();
    mStampAdapter = new StampAdapter(mStampItems, this);
    mRecyclerStampView.setAdapter(mStampAdapter);
  }

  private void getStampFromAlbum() {
    Logger.d(TAG, "getAlbum()");
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
    Logger.d(TAG, "start Activity : album intent");
    startActivityForResult(intent, REQUEST_TAKE_STAMP_FROM_ALBUM);
  }
}
