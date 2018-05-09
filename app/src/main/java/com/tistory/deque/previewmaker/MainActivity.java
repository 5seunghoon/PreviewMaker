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
import java.util.Locale;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity
  implements NavigationView.OnNavigationItemSelectedListener {
  public final static String PREVIEW_SAVED_DIRECTORY = "Preview Maker";
  public final static String STAMP_SAVED_DIRECTORY = "Stamp";

  private final int REQUEST_TAKE_STAMP_FROM_ALBUM = 101;
  private final int REQUEST_MAKE_STAMP_ACTIVITY = 102;
  private final int REQUEST_TAKE_PREVIEW_FROM_ALBUM = 103;
  private final String TAG = "MainActivity";

  private DBOpenHelper dbOpenHelper;

  private Toolbar mToolbar;
  private Permission mPermission;
  private TextView mMainActivityHintTextView;

  private String mCurrentPhotoPath;
  private Uri mCropSourceURI, mCropEndURI; //  mCropSourceURI = 자를 uri, mCropEndURI = 자르고 난뒤 uri

  private RecyclerView mRecyclerStampView;
  private ArrayList<StampItem> mStampItems;
  private StampAdapter mStampAdapter;
  private LinearLayoutManager mRecyclerViewLayoutManager;

  private ArrayList<String> mSeletedPreviews;


  private long mBackPressedTime;
  private int stampPosition;


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
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(!mPermission.checkPermissions()) return;
        getStampFromAlbum();
      }
    });

    setTitle("프리뷰 메이커");

    //setting drawer
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    //setting navigation view
    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);


    mPermission.checkPermissions();

    stampsFromDBToList();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode){
      case REQUEST_TAKE_STAMP_FROM_ALBUM: // 앨범에서 stamp선택 완료
        if(resultCode == Activity.RESULT_OK){
          File albumFile = createImageFile();
          mCropSourceURI = data.getData();
          mCropEndURI = Uri.fromFile(albumFile);
          nonCropImage();

          Logger.d(TAG, "mCropSourceURI : " + mCropSourceURI);
          Logger.d(TAG, "TAKE STAMP FROM ALBUM SUCCESS");
        } else {
          Logger.d(TAG, "TAKE STAMP FROM ALBUM FAIL");
        }
        break;

      case REQUEST_MAKE_STAMP_ACTIVITY: // 앨범 이름 등 설정 완료
        if(resultCode == Activity.RESULT_OK){
          addStampToListAndDB(data);
        }
        break;

      case REQUEST_TAKE_PREVIEW_FROM_ALBUM: // 앨범에서 preview들 선택 완료
        if(resultCode == Activity.RESULT_OK){
          List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

          Intent intent = new Intent(getApplicationContext(), PreviewEditActivity.class);
          intent.putStringArrayListExtra(PreviewEditActivity.EXTRA_PREVIEW_LIST, (ArrayList<String>) path);
          intent.setData(mStampItems.get(stampPosition).getImageURI());
          intent.putExtra(PreviewEditActivity.EXTRA_STAMP_ID, mStampItems.get(stampPosition).getID());
          startActivity(intent);
        }
        break;
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) { // 드로어가 열려있으면 닫고
      drawer.closeDrawer(GravityCompat.START);
    } else { // 드로어가 닫혀있으면 앱 종료
      if(System.currentTimeMillis() - mBackPressedTime > 2000){
        Snackbar.make(mToolbar, getString(R.string.snackbar_main_acti_back_to_exit), Snackbar.LENGTH_LONG)
          .setAction(getString(R.string.snackbar_main_acti_back_to_exit_btn), new View.OnClickListener() {
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
      , DBOpenHelper.DP_OPEN_HELPER_NAME
      , null
      , DBOpenHelper.dbVersion);
    dbOpenHelper.dbOpen();
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

  public File createImageFile() {
    Logger.d(TAG, "createImageFile func");
    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA).format(new Date());
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

  public void nonCropImage(){
    /**
     * copy mCropSourceURI and paste to mCropEndURI
     */
    String pathCropSourceURI = getRealPathFromURI(mCropSourceURI);
    File file = new File(pathCropSourceURI);
    File outFile = new File(mCropEndURI.getPath());
    Logger.d(TAG, "inFile , outFile " + file + " , " + outFile);

    if (file.exists()) {

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
      } catch (IOException e) {
        Logger.d(TAG, "FILE COPY FAIL");
        Snackbar.make(this.getCurrentFocus(), getString(R.string.snackbar_main_acti_stamp_copy_err), Snackbar.LENGTH_LONG);
        e.printStackTrace();
      }
    } else {
      Logger.d(TAG, "IN FILE NOT EXIST");
    }

    galleryAddPic(this, mCropEndURI.getPath());

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

  public static void galleryAddPic(Activity activity, String imagePath) {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(imagePath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    activity.sendBroadcast(mediaScanIntent);
  }
  public static void galleryAddPic(Activity activity, Uri imageURI){
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    mediaScanIntent.setData(imageURI);
    activity.sendBroadcast(mediaScanIntent);
  }

  private void addStampToListAndDB(Intent data){
    dbOpenHelper.dbInsertStamp(data.getStringExtra(MakeStampActivity.STAMP_NAME), data.getData());

    final String MY_QUERY = "SELECT MAX(_id) FROM " + dbOpenHelper.TABLE_NAME_STAMPS;
    Cursor cur = dbOpenHelper.db.rawQuery(MY_QUERY, null);
    cur.moveToFirst();
    int maxID = cur.getInt(0);

    mStampItems.add(new StampItem(maxID, data.getData(), data.getStringExtra(MakeStampActivity.STAMP_NAME)));
    Logger.d(TAG, "INSERT : ID : " + maxID + " imageURI : " + data.getData() + " name : " + data.getStringExtra(MakeStampActivity.STAMP_NAME));

    mStampAdapter.notifyDataSetChanged();
    invisibleHint();
  }

  protected void callFromListItem(int stampPosition){
    getPreviewsFromAlbum();
    this.stampPosition = stampPosition;
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
        galleryAddPic(this, imageURI);
        Logger.d(TAG, "media scanning end");

        try{
          mStampItems.remove(position);
        } catch (IndexOutOfBoundsException e){
          Logger.d(TAG, "out ouf bound");
        }

        mStampAdapter.notifyDataSetChanged();

        dbOpenHelper.dbDeleteStamp(id);

        //viewEveryItemInDB();

        visibleHint();

        Snackbar.make(v, "[" + name + "] 삭제 완료", Snackbar.LENGTH_LONG).show();
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
    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 20);
    // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
    // default select images (support array list)
    intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSeletedPreviews);
    startActivityForResult(intent, REQUEST_TAKE_PREVIEW_FROM_ALBUM);
  }

  private void stampsFromDBToList() {
    /**
     * Read DB, on List
     * DB에서 stamp를 전부 읽어서 리스트로 불러옴
     * 이때 DB에 있는 stamp의 실제 파일이 존재하지 않을 경우 DB에서 삭제함
     */
    int id, width, height, posWidthPer, posHeightPer;
    String imageURIPath, name;
    String sql = "SELECT * FROM " + dbOpenHelper.TABLE_NAME_STAMPS + ";";
    Cursor results = dbOpenHelper.db.rawQuery(sql, null);

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
      File stampFile = new File(Uri.parse(imageURIPath).getPath());
      Logger.d(TAG, " imageURIPath : " + imageURIPath );
      if (!stampFile.exists()) {
        dbOpenHelper.dbDeleteStamp(id);
      } else {
        mStampItems.add(new StampItem(id, Uri.parse(imageURIPath), name, width, height, posWidthPer, posHeightPer));
      }
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

}
