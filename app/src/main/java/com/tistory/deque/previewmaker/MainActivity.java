package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.ClipData;
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
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
  implements NavigationView.OnNavigationItemSelectedListener {
  private final int REQUEST_TAKE_STAMP_FROM_ALBUM = 101;
  private final int REQUEST_IMAGE_CROP = 102;
  private final int REQUEST_MAKE_STAMP_ACTIVITY = 103;
  private final int REQUEST_TAKE_PREVIEW_FROM_ALBUM = 104;
  private final String TAG = "MainActivity";

  DBOpenHelper dbOpenHelper;
  int dbVersion = 1;
  final String dpOpenHelperName = "DB_OPEN_HELPER_NAME";
  /**
   * 중요 :
   * endOfID는 ArrayList의 Stamp들과 DB의 item들이 서로 같은 id를 갖게 하기 위해
   * 존재하는 변수.
   *
   * endOfID는 처음 앱이 실행될때 DB에 있는 아이템들의 id를 체크하여 가장 높은
   * id를 체크하고있다. 그리고 새로운 stamp를 만들면 그 endOfID + 1 값을 id로 가지고,
   * 그 값이 또한 DB에 들어가게 됨으로써 arrayList의 stamp들과 DB들의 item들의 id가
   * 싱크가 맞춰진다. 즉, arrayList의 stamp중 하나를 골라 id를 뽑아낸 다음, DB에서
   * 해당 id를 찾으면 그것은 방금 고른 stamp와 같은 item이 된다.
   *
   * 따라서 ArryaList에서 Stamp를 하나 삭제하게 되면,
   * 그것과 같은 Stamp를 DB에서 삭제해야 한다.
   * 즉, 방금 삭제한 ArrayList에 있던 Stamp의 ID를 식별하고,
   * 그 ID와 같은 item을 DB에서 찾은 뒤 그 item을 삭제하면 된다.
   *
   * 식별자를 이름과 같은 걸 쓰지않고 ID를 내장하여 쓰는 이유는 이름을 중복해서 만들수 있게 하기 위해서임.
   * 다만 endOfID가 DB에 있는 ID와 중복되는 경우를 예외처리하지 않았음...
   */
  int endOfID = 0; // db에 있는 id들의 값들 중 가장 큰 값

  Toolbar mToolbar;
  Permission mPermission;

  String mCurrentPhotoPath;
  Uri mCropSourceURI, mCropEndURI; //  mCropSourceURI = 자를 uri, mCropEndURI = 자르고 난뒤 uri

  RecyclerView mRecyclerStampView;
  ArrayList<StampItem> mStampItems;
  StampAdatper mStampAdapter;
  LinearLayoutManager mRecyclerViewLayoutManager;



  long mBackPressedTime;
  int position;

  //ImageView imageView;

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


  private void dbOpen(){
    dbOpenHelper = DBOpenHelper.getDbOpenHelper(
      getApplicationContext()
    , dpOpenHelperName
    , null
    , dbVersion);
    dbOpenHelper.dbOpen();
  }

  private void stampsFromDBToList() {
    int id;
    String imageURI;
    String name;
    String sql = "SELECT * FROM " + dbOpenHelper.TABLE_NAME_STAMPS + ";";
    Cursor results = null;
    results = dbOpenHelper.db.rawQuery(sql, null);
    Log.d(TAG, "Cursor open");
    results.moveToFirst();
    while(!results.isAfterLast()) {
      id = results.getInt(0);
      name = results.getString(1);
      imageURI = results.getString(2);
      Log.d(TAG, "DB ITEM : id : " + id + " imageURI : " + imageURI + " name : " + name);
      mStampItems.add(new StampItem(id, Uri.parse(imageURI), name));
      if(id > endOfID) endOfID = id;

      results.moveToNext();
    }
  }

  private void setRecyclerView(){

    mRecyclerStampView = findViewById(R.id.recyclerStampView);
    // 각 Item 들이 RecyclerView 의 전체 크기를 변경하지 않는 다면
    // setHasFixedSize() 함수를 사용해서 성능을 개선할 수 있습니다.
    // 변경될 가능성이 있다면 false 로 , 없다면 true를 설정해주세요.
    mRecyclerStampView.setHasFixedSize(true);

    mRecyclerViewLayoutManager = new LinearLayoutManager(this);
    mRecyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerStampView.setLayoutManager(mRecyclerViewLayoutManager);
    mRecyclerStampView.setItemAnimator(new DefaultItemAnimator());

    mStampItems = new ArrayList<>();
    mStampAdapter = new StampAdatper(mStampItems, this, dbOpenHelper);
    mRecyclerStampView.setAdapter(mStampAdapter);


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
          mCropSourceURI = data.getData();
          Log.d(TAG, "mCropSourceURI : " + mCropSourceURI);
          mCropEndURI = Uri.fromFile(albumFile);

          //cropImage();
          nonCropImage();

          Log.d(TAG, "TAKE STAMP FROM ALBUM OK");
        } else {
          Log.d(TAG, "TAKE STAMP FROM ALBUM FAIL");
        }
        break;

      case REQUEST_IMAGE_CROP:
        if(resultCode == Activity.RESULT_OK){
          galleryAddPic();
          Intent intent = new Intent(getApplicationContext(), MakeStampActivity.class);
          intent.setData(mCropEndURI);
          startActivityForResult(intent, REQUEST_MAKE_STAMP_ACTIVITY);
          Log.d(TAG, "IMAGE CROP OK");
        } else if(resultCode == Activity.RESULT_CANCELED){
          Log.d(TAG, "IMAGE CROP CANCLE");
        } else {
          Log.d(TAG, "IMAGE CROP FIRST USER");
        }
        break;

      case REQUEST_MAKE_STAMP_ACTIVITY:
        if(resultCode == Activity.RESULT_OK){
          //imageView.setImageURI(data.getData());
          //TextView textView = findViewById(R.id.tempTextView);
          //textView.setText(data.getStringExtra("STAMP_NAME"));
          endOfID++;
          mStampItems.add(new StampItem(endOfID, data.getData(), data.getStringExtra("STAMP_NAME")));
          dbOpenHelper.dbInsertStamp(data.getStringExtra("STAMP_NAME"), data.getData());
          Log.d(TAG, "INSERT : ID : " + endOfID + " imageURI : " + data.getData() + " name : " + data.getStringExtra("STAMP_NAME"));
          mStampAdapter.notifyDataSetChanged();
        }
        break;

      case REQUEST_TAKE_PREVIEW_FROM_ALBUM:
        if(resultCode == Activity.RESULT_OK){
          Intent intent = new Intent(getApplicationContext(), PreviewEditActivity.class);
          intent.setClipData(data.getClipData());
          intent.setData(mStampItems.get(position).getImageURI());
          intent.putExtra("STAMP_ID", mStampItems.get(position).getID());
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

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    mPermission.requestPermissionsResult(requestCode, permissions, grantResults);
  }

  public File createImageFile() throws IOException {
    Log.d(TAG, "createImageFile func");
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "STAMP_" + timeStamp + ".png";
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
     * mCropSourceURI = 자를 uri
     * mCropEndURI = 자르고 난뒤 uri
     */
    Log.d(TAG, "cropImage() CALL");
    Log.d(TAG, "cropImage() : Photo URI, Album URI" + mCropSourceURI + ", " + mCropEndURI);

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
    Log.d(TAG, "inFile , outFile " + file + " , " + outFile);

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
        Log.d(TAG, "FILE COPY FAIL");
        e.printStackTrace();
      }
    } else {
      Log.d(TAG, "IN FILE NOT EXIST");
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

    Log.d(TAG, "getRealPathFromURI(), path : " + uri.toString());

    cursor.close();
    return path;
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

  protected void callFromListItem(int position){
    getPreviewsFromAlbum();
    this.position = position;
  }

  private void getPreviewsFromAlbum(){
    Log.d(TAG, "getPreviewsFromAlbum()");
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
    Log.d(TAG, "start Activity : album intent");
    startActivityForResult(intent, REQUEST_TAKE_PREVIEW_FROM_ALBUM);
  }

}
