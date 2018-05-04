package com.tistory.deque.previewmaker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PreviewEditActivity extends AppCompatActivity {

  private final String TAG = "PreviewEditActivity";

  public static final String URI_ERROR = "URI_ERROR";
  public static final String EXTRA_STAMP_ID = "STAMP_ID";
  public static final String EXTRA_STAMP_POSITION = "STAMP_POSITION";
  public static final String EXTRA_PREVIEW_LIST = "PREVIEW_LIST";
  final String dpOpenHelperName = "DB_OPEN_HELPER_NAME";

  public static int canvasGrandParentViewWidth, canvasGrandParentViewHeight;

  protected static int POSITION = -1;
  private boolean isClickPreviewFirst = false;
  long mBackPressedTime;

  int stampID;
  Uri stampImageURI;

  private DBOpenHelper dbOpenHelper;
  private StampItem selectedStamp;

  ArrayList<String> previewPaths;
  ArrayList<PreviewItem> previewItems;
  private RecyclerView mRecyclerPreviewView;
  private PreviewAdapter mPreviewAdapter;
  private LinearLayoutManager mRecyclerPreviewViewLayoutManager;

  private LinearLayout mCanvasPerantLayout;
  private LinearLayout mCanvasGrandParentLayout;
  private PreviewCanvasView mPreviewCanvasView;
  protected ProgressBar previewLoadingProgressBar;

  private TextView canvasviewHintTextView;

  private Button mButtonSaveAll, mButtonCrop, mButtonStamp, mButtonEmoticon, mButtonDelete;

  StampItem stamp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preview_edit);

    canvasviewHintTextView = findViewById(R.id.canvasviewHintTextView);
    previewLoadingProgressBar = findViewById(R.id.previewLoadingProgressBar);
    POSITION = -1;

    Intent intent = getIntent();
    stampID = intent.getExtras().getInt("STAMP_ID");
    stampImageURI = intent.getData();
    previewPaths = new ArrayList<>();
    previewItems = new ArrayList<>();
    previewPaths = intent.getStringArrayListExtra(EXTRA_PREVIEW_LIST);
    setTitle(R.string.title_preview_make_activity);

    setRecyclerView();
    setPreviewCanvas();
    setButtonListener();
    setStamp(stampID);


    LoadingPreviewThumbnail loadingPreviewThumbnail = new LoadingPreviewThumbnail();
    loadingPreviewThumbnail.execute(this);

  }

  @Override
  public void onBackPressed() {
    if (System.currentTimeMillis() - mBackPressedTime > 2000) {
      Snackbar.make(getCurrentFocus(), "편집을 취소하려면 뒤로 버튼을 한번 더 눌려주세요.\n저장되지 않을 수 있어요.", Snackbar.LENGTH_LONG)
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode){
      case UCrop.REQUEST_CROP:
        if (resultCode == RESULT_OK) {
          final Uri resultUri = UCrop.getOutput(data);

          Logger.d(TAG, "result URI : " + resultUri);
          Logger.d(TAG, "result path : " + resultUri.getPath());

          Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
          mediaScanIntent.setData(resultUri);
          sendBroadcast(mediaScanIntent);

          PreviewItem previewItem = previewItems.get(POSITION);
          previewItem.setOriginalImageURI(resultUri);
          previewItem.cropped();

        } else if (resultCode == UCrop.RESULT_ERROR) {
          final Throwable cropError = UCrop.getError(data);
          Logger.d(TAG, "CROP ERROR");
        } else {
          Logger.d(TAG, "CROP CANCEL");
        }
    }
  }

  private void setStamp(int stampID){
    dbOpenHelper = DBOpenHelper.getDbOpenHelper(
      getApplicationContext()
      , DBOpenHelper.dpOpenHelperName
      , null
      , DBOpenHelper.dbVersion);
    dbOpenHelper.dbOpen();

    try{
      selectedStamp = stampsFromDB(stampID);
    } catch (FileNotFoundException e){
      e.printStackTrace();
    }
  }

  public void stampUpdate(int id, int width, int height, int posWidthPer, int posHeightPer){
    String sql = "UPDATE " + dbOpenHelper.TABLE_NAME_STAMPS + " SET "
      + dbOpenHelper.STAMP_WIDTH_KEY + " = " + width
      + ", "
      + dbOpenHelper.STAMP_HEIGHT_KEY + " = " + height
      + ", "
      + dbOpenHelper.STAMP_POS_WIDTH_PERCENT_KEY + " = " + posWidthPer
      + ", "
      + dbOpenHelper.STAMP_POS_HEIGHT_PERCENT_KEY + " = " + posHeightPer
      + " WHERE _ID IN(" + stampID + ")" + ";";
    dbOpenHelper.db.rawQuery(sql, null);
  }

  public void stampWidthHeightUpdate(int id, int width, int height){
    String sql = "UPDATE " + dbOpenHelper.TABLE_NAME_STAMPS + " SET "
      + dbOpenHelper.STAMP_WIDTH_KEY + " = " + width
      + ", "
      + dbOpenHelper.STAMP_HEIGHT_KEY + " = " + height
      + " WHERE _ID IN(" + stampID + ")" + ";";
    dbOpenHelper.db.rawQuery(sql, null);
  }

  private StampItem stampsFromDB(int stampID) throws FileNotFoundException{
    int id;
    String imageURIPath;
    String name;
    int width, height, posWidthPer, posHeightPer;
    String sql = "SELECT * FROM " + dbOpenHelper.TABLE_NAME_STAMPS + " WHERE _ID IN(" + stampID + ")" + ";";
    Cursor results = null;
    results = dbOpenHelper.db.rawQuery(sql, null);
    Logger.d(TAG, "Cursor open sql : " + sql);

    results.moveToFirst();
    id = results.getInt(0);
    name = results.getString(1);
    imageURIPath = results.getString(2);
    width = results.getInt(3);
    height = results.getInt(4);
    posWidthPer = results.getInt(5);
    posHeightPer = results.getInt(6);
    Logger.d(TAG, "STAMP FIND SUCCESS : id : " + id + " imageURIPath : " + imageURIPath + " name : " + name);

    String imageURIFilePath = Uri.parse(imageURIPath).getPath();
    File stampFile = new File(imageURIFilePath);

    if (stampFile.exists()) {
      return new StampItem(id, Uri.parse(imageURIPath), name, width, height, posWidthPer, posHeightPer);
    } else {
      throw new FileNotFoundException();
    }

  }

  public void setButtonListener(){
    mButtonSaveAll = findViewById(R.id.buttonSaveAll);
    mButtonCrop = findViewById(R.id.buttonCrop);
    mButtonStamp = findViewById(R.id.buttonStamp);
    mButtonEmoticon = findViewById(R.id.buttonEmoticon);
    mButtonDelete = findViewById(R.id.buttonDelete);

    mButtonSaveAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonSaveAll();
      }
    });
    mButtonCrop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonCrop();
      }
    });
    mButtonStamp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonStamp();
      }
    });
    mButtonEmoticon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonEmoticon();
      }
    });
    mButtonDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonDelete();
      }
    });
  }

  public void clickButtonSaveAll(){
    mPreviewCanvasView.savePreview();
  }
  public void clickButtonCrop(){
    if(POSITION < 0 || POSITION >= previewItems.size()){
      return;
    }
    Logger.d(TAG, "Click crop button");
    Uri destURI = previewItems.get(POSITION).getResultImageURI();
    Uri origURI = previewItems.get(POSITION).getOriginalImageURI();

    UCrop.Options options = setCropViewOption();
    UCrop.of(origURI, destURI)
      .withOptions(options)
      .start(this);

    Logger.d(TAG, "crop start : orig : " + origURI +", dest : " + destURI);
  }
  public void clickButtonStamp(){
    mPreviewCanvasView.setStampShown(true);
    if(selectedStamp != null) mPreviewCanvasView.setStampItem(selectedStamp);
    mPreviewCanvasView.callInvalidate();
  }
  public void clickButtonEmoticon(){
  }
  public void clickButtonDelete(){

  }

  private UCrop.Options setCropViewOption(){
    UCrop.Options options = new UCrop.Options();
    options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));
    options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.black));
    options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));


    options.setAspectRatioOptions(1,
      new AspectRatio("16:9", 16, 9),
      new AspectRatio("3:2", 3, 2),
      new AspectRatio("ORIGINAL", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
      new AspectRatio("1:1", 1, 1),
      new AspectRatio("2:3", 2, 3),
      new AspectRatio("9:16", 9, 16)
    );

    return options;
  }

  private void setPreviewCanvas(){
    mCanvasGrandParentLayout = findViewById(R.id.canvasGrandParentLayout);
    mCanvasPerantLayout = findViewById(R.id.canvasParentLayout);
    mPreviewCanvasView = new PreviewCanvasView(this, this, previewItems);
    mCanvasPerantLayout.addView(mPreviewCanvasView);


    mCanvasGrandParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        canvasGrandParentViewWidth = mCanvasGrandParentLayout.getWidth();
        canvasGrandParentViewHeight = mCanvasGrandParentLayout.getHeight();
        //int min = canvasGrandParentViewWidth < canvasGrandParentViewHeight ? canvasGrandParentViewWidth : canvasGrandParentViewHeight;
        //min -= 50;
        PreviewCanvasView.grandParentWidth = canvasGrandParentViewWidth;
        PreviewCanvasView.grandParentHeight = canvasGrandParentViewHeight;
        //Logger.d(TAG, "W : " + canvasGrandParentViewWidth + ", H : " + canvasGrandParentViewHeight + ", min : " + min);
      }
    });

  }
  private void setRecyclerView(){
    mRecyclerPreviewView = findViewById(R.id.previewRecyclerView);
    mRecyclerPreviewView.setHasFixedSize(true);

    mRecyclerPreviewViewLayoutManager = new LinearLayoutManager(this);
    mRecyclerPreviewViewLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    mRecyclerPreviewView.setLayoutManager(mRecyclerPreviewViewLayoutManager);
    mRecyclerPreviewView.setItemAnimator(new DefaultItemAnimator());

    mPreviewAdapter = new PreviewAdapter(this, previewItems);
    mRecyclerPreviewView.setAdapter(mPreviewAdapter);
  }

  protected void clickPreviewItem(View v, int position){
    if(POSITION != position) mPreviewCanvasView.clickNewPreview();
    POSITION = position;
    mPreviewCanvasView.callInvalidate();
    if(!isClickPreviewFirst){
      isClickPreviewFirst = true;
      canvasviewHintTextView.setVisibility(View.GONE);
      mCanvasPerantLayout.setVisibility(View.VISIBLE);
    }
  }

  public Uri getUriFromPath(String filePath) {
    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      null, "_data = '" + filePath + "'", null, null);

    cursor.moveToNext();
    int id = cursor.getInt(cursor.getColumnIndex("_id"));
    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

    return uri;
  }

  public Uri thumbnailURIFromOriginalURI(String path) {
    path = Uri.parse(path).getPath();
    Logger.d(TAG, "thumbnail path : " + path);
    Uri selectedImageUri = getUriFromPath(path);
    long rowId = Long.valueOf(selectedImageUri.getLastPathSegment());
    Logger.d(TAG, "original uri : " + selectedImageUri + " , row ID : " + rowId);
    return imageIdToThumbnail(""+ rowId);
  }

  public Uri imageIdToThumbnail(String imageId) {
    String[] projection = { MediaStore.Images.Thumbnails.DATA };
    ContentResolver contentResolver = getContentResolver();

    Cursor thumbnailCursor = contentResolver.query(
      MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
      projection,
      MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
      new String[]{imageId},
      null);
    if (thumbnailCursor == null) {
      return null;
    } else if (thumbnailCursor.moveToFirst()) {
      int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);

      String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
      thumbnailCursor.close();
      return Uri.parse(thumbnailPath);
    } else {
      MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
      thumbnailCursor.close();
      Logger.d(TAG, "No exist thumbnail, so make it");
      return imageIdToThumbnail(imageId);
    }
  }

  protected class LoadingPreviewThumbnail extends AsyncTask<PreviewEditActivity, Integer, Boolean>{

    private double loadingCounter = 0;

    @Override
    protected void onPreExecute() { // 스레드 실행 전
      super.onPreExecute();
      previewLoadingProgressBar.setVisibility(View.VISIBLE);
      previewLoadingProgressBar.setMax(100);
      Logger.d(TAG, "async task execute");
    }

    @Override
    protected Boolean doInBackground(PreviewEditActivity... param) { // 스래드 실행 중
      Uri thumbnailUri, originalUri;

      for (String previewPath : previewPaths) { // previewPaths -> previewItems
        thumbnailUri = thumbnailURIFromOriginalURI(previewPath);
        originalUri = getUriFromPath(previewPath);
        if(thumbnailUri == null){
          thumbnailUri = originalUri;
          Logger.d(TAG, "Thumbnail parsing error");
        }
        Logger.d(TAG, "Thumbnail parsing success : " + thumbnailUri);
        previewItems.add(new PreviewItem(originalUri, thumbnailUri, param[0]));
        Logger.d(TAG, "previewItem success : " + originalUri);

        loadingCounter++;
        publishProgress();
      }
      return Boolean.TRUE;
    }

    @Override
    protected void onProgressUpdate(Integer... values) { // 중간 업데이트
      super.onProgressUpdate(values);
      mPreviewAdapter.notifyItemInserted((int)loadingCounter);
      double size =  previewPaths.size();
      double progress = loadingCounter / size;
      previewLoadingProgressBar.setProgress((int)(100.0 * progress));
    }

    @Override
    protected void onPostExecute(Boolean result) { // 실행 완료
      super.onPostExecute(result);
      previewLoadingProgressBar.setVisibility(View.GONE);
    }
  }

}
