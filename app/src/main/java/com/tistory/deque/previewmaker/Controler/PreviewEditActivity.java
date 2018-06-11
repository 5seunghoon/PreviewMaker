package com.tistory.deque.previewmaker.Controler;

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
import android.widget.SeekBar;
import android.widget.TextView;

import com.tistory.deque.previewmaker.Model_Global.ClickState;
import com.tistory.deque.previewmaker.Model_Global.ClickStateEnum;
import com.tistory.deque.previewmaker.Model_Global.DBOpenHelper;
import com.tistory.deque.previewmaker.Model_Global.StampEditSelectedEnum;
import com.tistory.deque.previewmaker.Model_Global.StampSeekBarListener;
import com.tistory.deque.previewmaker.Model_PreviewData.PreviewAdapter;
import com.tistory.deque.previewmaker.Model_PreviewData.PreviewItem;
import com.tistory.deque.previewmaker.R;
import com.tistory.deque.previewmaker.Model_StampData.StampItem;
import com.tistory.deque.previewmaker.Util.Logger;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PreviewEditActivity extends AppCompatActivity {

  private final String TAG = "PreviewEditActivity";

  public static final String EXTRA_STAMP_ID = "STAMP_ID";
  public static final String EXTRA_PREVIEW_LIST = "PREVIEW_LIST";
  public static int SeekBarBrightnessMax = 512;

  public static int canvasGrandParentViewWidth, canvasGrandParentViewHeight;

  protected static int POSITION = -1;
  private boolean isClickPreviewFirst = false;
  private long mBackPressedTime;

  private int stampID;
  private Uri stampImageURI;

  private DBOpenHelper dbOpenHelper;
  private StampItem selectedStamp;

  private ArrayList<String> previewPaths;
  private ArrayList<PreviewItem> previewItems;
  private RecyclerView mRecyclerPreviewView;
  private PreviewAdapter mPreviewAdapter;
  private LinearLayoutManager mRecyclerPreviewViewLayoutManager;

  private LinearLayout mCanvasPerantLayout;
  private LinearLayout mCanvasGrandParentLayout;
  private PreviewCanvasView mPreviewCanvasView;
  protected ProgressBar previewLoadingProgressBar;
  private LinearLayout layoutEditButton;
  private LinearLayout layoutStampEditButton;

  private TextView canvasviewHintTextView;

  private Button mButtonSaveEach, mButtonCrop, mButtonStamp, mButtonDelete;
  private Button mButtonStampFinish, mButtonStampDelete, mButtonStampBrightness, mButtonStampReset;

  private SeekBar mStampSeekBar;
  private StampSeekBarListener mStampSeekBarBrightnessListener;
  private LinearLayout mLayoutStampEditSeekBar;
  private TextView mStampSeekBarTextView;

  public PreviewCanvasView getmPreviewCanvasView() {
    return mPreviewCanvasView;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preview_edit);

    canvasviewHintTextView = findViewById(R.id.canvasviewHintTextView);
    previewLoadingProgressBar = findViewById(R.id.previewLoadingProgressBar);
    layoutEditButton = findViewById(R.id.layoutEditButton);
    layoutStampEditButton = findViewById(R.id.layoutStampEditButton);

    POSITION = -1;
    isClickPreviewFirst = false;

    Intent intent = getIntent();
    stampID = intent.getExtras().getInt(EXTRA_STAMP_ID);
    stampImageURI = intent.getData();
    previewPaths = new ArrayList<>();
    previewItems = new ArrayList<>();
    previewPaths = intent.getStringArrayListExtra(EXTRA_PREVIEW_LIST);
    setTitle(R.string.title_preview_make_activity);

    setRecyclerView();
    setPreviewCanvas();
    setButtonListener();
    setSeekBar();
    setStamp(stampID);

    setVisibleInit();


    LoadingPreviewThumbnail loadingPreviewThumbnail = new LoadingPreviewThumbnail();
    loadingPreviewThumbnail.execute(this);

  }

  @Override
  public void onBackPressed() {
    if (mPreviewCanvasView.backPressed()) return;
    if (System.currentTimeMillis() - mBackPressedTime > 2000) {
      Snackbar.make(getCurrentFocus(), getString(R.string.snackbar_preview_edit_acti_back_to_exit), Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.snackbar_preview_edit_acti_back_to_exit_btn), new View.OnClickListener() {
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
          previewItem.editted();

        } else if (resultCode == UCrop.RESULT_ERROR) {
          final Throwable cropError = UCrop.getError(data);
          Logger.d(TAG, "CROP ERROR");
        } else {
          Logger.d(TAG, "CROP CANCEL");
        }
    }
  }

  private void setStamp(int stampID) {
    dbOpenHelper = DBOpenHelper.getDbOpenHelper(
      getApplicationContext()
      , DBOpenHelper.DP_OPEN_HELPER_NAME
      , null
      , DBOpenHelper.dbVersion);
    dbOpenHelper.dbOpen();

    try{
      selectedStamp = stampsFromDB(stampID);
    } catch (FileNotFoundException e){
      e.printStackTrace();
    }
  }

  public void stampUpdate(int id, int width, int height, int posWidthPer, int posHeightPer, int anchorInt) {
    dbOpenHelper.dbUpdateStamp(id, width, height, posWidthPer, posHeightPer, anchorInt);
  }

  private StampItem stampsFromDB(int stampID) throws FileNotFoundException{
    int id, width, height, posWidthPer, posHeightPer, anchorInt;
    String imageURIPath, name;

    String sql = "SELECT * FROM " + dbOpenHelper.TABLE_NAME_STAMPS + " WHERE _ID IN(" + stampID + ")" + ";";
    Cursor results = dbOpenHelper.db.rawQuery(sql, null);
    Logger.d(TAG, "Cursor open sql : " + sql);

    results.moveToFirst();
    id = results.getInt(0);
    name = results.getString(1);
    imageURIPath = results.getString(2);
    width = results.getInt(3);
    height = results.getInt(4);
    posWidthPer = results.getInt(5);
    posHeightPer = results.getInt(6);
    anchorInt = results.getInt(7);

    Logger.d(TAG, "STAMP FIND SUCCESS : id : " + id + " imageURIPath : " + imageURIPath + " name : " + name);

    String imageURIFilePath = Uri.parse(imageURIPath).getPath();
    File stampFile = new File(imageURIFilePath);

    if (stampFile.exists()) {
      return new StampItem(id, Uri.parse(imageURIPath), name, width, height, posWidthPer, posHeightPer, anchorInt);
    } else {
      throw new FileNotFoundException();
    }

  }

  public void setButtonListener() {
    mButtonCrop = findViewById(R.id.buttonCrop);
    mButtonStamp = findViewById(R.id.buttonStamp);
    mButtonDelete = findViewById(R.id.buttonDelete);
    mButtonSaveEach = findViewById(R.id.buttonSaveEach);

    mButtonStampFinish = findViewById(R.id.buttonStampFinish);
    mButtonStampDelete = findViewById(R.id.buttonStampDelete);
    mButtonStampBrightness = findViewById(R.id.buttonStampBrightness);
    mButtonStampReset = findViewById(R.id.buttonStampReset);

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
    mButtonDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonDelete();
      }
    });
    mButtonSaveEach.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonSaveEach();
      }
    });

    mButtonStampFinish.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonStampFinish();
      }
    });
    mButtonStampDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonStampDelete();
      }
    });
    mButtonStampBrightness.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonStampBrightness();
      }
    });
    mButtonStampReset.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickButtonStampReset();
      }
    });
  }

  private void setSeekBar() {
    mLayoutStampEditSeekBar = findViewById(R.id.layoutStampEditSeekBar);
    mStampSeekBarTextView = findViewById(R.id.stampSeekBarTextView);
    mStampSeekBarBrightnessListener = new StampSeekBarListener(this, StampEditSelectedEnum.BRIGHTNESS, mPreviewCanvasView);
    mStampSeekBar = findViewById(R.id.stampEditSeekBar);
    mStampSeekBar.setMax(SeekBarBrightnessMax);
    mStampSeekBar.setProgress(SeekBarBrightnessMax / 2);
  }

  private void setVisibleInit() {
    layoutStampEditButton.setVisibility(View.GONE);
    layoutEditButton.setVisibility(View.VISIBLE);
    mLayoutStampEditSeekBar.setVisibility(View.INVISIBLE);
  }


  public void clickButtonSaveEach() {
    mPreviewCanvasView.savePreviewEach(-1);
  }

  public void clickButtonCrop() {
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

  private UCrop.Options setCropViewOption() {
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

  public void clickButtonStamp() {
    if(POSITION < 0) return;

    if (!mPreviewCanvasView.isStampShown()) {
      mPreviewCanvasView.showStamp();
      if (selectedStamp != null) {
        mPreviewCanvasView.setStampItem(selectedStamp);
      }
      mPreviewCanvasView.callInvalidate();
    }
    else{
      mPreviewCanvasView.clickStamp();
    }
  }

  public void clickButtonDelete() {
    mLayoutStampEditSeekBar.setVisibility(View.INVISIBLE);

    if(previewItems.size() == 1) return;

    int removePos = POSITION;

    mPreviewAdapter.notifyItemRemoved(removePos);
    mPreviewAdapter.notifyItemRangeChanged(0, previewItems.size());

    if(POSITION == previewItems.size() - 1) { // end of list
      POSITION = 0;
    }
    previewItems.remove(removePos);
    previewPaths.remove(removePos);

    mPreviewCanvasView.callInvalidate();
  }

  public void clickButtonStampFinish() {
    mLayoutStampEditSeekBar.setVisibility(View.INVISIBLE);
    mPreviewCanvasView.finishStampEdit();
  }

  public void clickButtonStampDelete() {
    mLayoutStampEditSeekBar.setVisibility(View.INVISIBLE);
    mPreviewCanvasView.deleteStamp();
  }

  public void clickButtonStampBrightness() {
    mLayoutStampEditSeekBar.setVisibility(View.VISIBLE);
    mStampSeekBar.setProgress(selectedStamp.getBrightness());
    mStampSeekBar.setOnSeekBarChangeListener(mStampSeekBarBrightnessListener);
    mPreviewCanvasView.brightnessStamp();
    setStampSeekBarText(selectedStamp.getBrightness(), StampEditSelectedEnum.BRIGHTNESS);
    mPreviewCanvasView.callInvalidate();
  }

  public void clickButtonStampReset(){
      mLayoutStampEditSeekBar.setVisibility(View.INVISIBLE);
    mPreviewCanvasView.stampReset();
  }

  public void setStampSeekBarText(int value, StampEditSelectedEnum selected){
    if(selected == StampEditSelectedEnum.BRIGHTNESS){
      int resultProgressValue = (int) ((value - PreviewEditActivity.SeekBarBrightnessMax / 2f) / (PreviewEditActivity.SeekBarBrightnessMax / 2f) * 100f);
      mStampSeekBarTextView.setText(resultProgressValue + "%");
    }
  }

  private void setPreviewCanvas() {
    mCanvasGrandParentLayout = findViewById(R.id.canvasGrandParentLayout);
    mCanvasPerantLayout = findViewById(R.id.canvasParentLayout);
    mPreviewCanvasView = new PreviewCanvasView(this, this, previewItems);
    mCanvasPerantLayout.addView(mPreviewCanvasView);

    mCanvasGrandParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        canvasGrandParentViewWidth = mCanvasGrandParentLayout.getWidth();
        canvasGrandParentViewHeight = mCanvasGrandParentLayout.getHeight();
        PreviewCanvasView.grandParentWidth = canvasGrandParentViewWidth;
        PreviewCanvasView.grandParentHeight = canvasGrandParentViewHeight;
      }
    });
  }

  private void setRecyclerView() {
    mRecyclerPreviewView = findViewById(R.id.previewRecyclerView);
    mRecyclerPreviewView.setHasFixedSize(true);

    mRecyclerPreviewViewLayoutManager = new LinearLayoutManager(this);
    mRecyclerPreviewViewLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    mRecyclerPreviewView.setLayoutManager(mRecyclerPreviewViewLayoutManager);
    mRecyclerPreviewView.setItemAnimator(new DefaultItemAnimator());

    mPreviewAdapter = new PreviewAdapter(this, previewItems);
    mRecyclerPreviewView.setAdapter(mPreviewAdapter);
  }

  public void clickPreviewItem(View v, int position) {
    if (mPreviewCanvasView.isNowEditingStamp()) return;

    if (POSITION != position) {
      mPreviewCanvasView.clickNewPreview(position);
    }

    if(!isClickPreviewFirst) {
      isClickPreviewFirst = true;
      canvasviewHintTextView.setVisibility(View.GONE);
      mCanvasPerantLayout.setVisibility(View.VISIBLE);
      mPreviewCanvasView.changeAndInitPreviewInCanvas(position);
    }
  }

  public void editButtonGoneOrVisible(ClickState CLICK_STATE) {
    if(CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_STAMP_EDIT ||
       CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_STAMP_ZOOM ){
      layoutEditButton.setVisibility(View.GONE);
      layoutStampEditButton.setVisibility(View.VISIBLE);
    }
    else{
      layoutStampEditButton.setVisibility(View.GONE);
      layoutEditButton.setVisibility(View.VISIBLE);
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

  private void viewEveryItemInDB() {
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
      Logger.d(TAG, "DB ITEM : id : " + _id + " imageURI : " + _imageURI + " name : " + _name +
        " W : " + results.getString(3) + " H : " + results.getString(4) + " W P : " + results.getString(5) + " H P : "  +results.getString(6));
      results.moveToNext();
    }
  }

  protected class LoadingPreviewThumbnail extends AsyncTask<PreviewEditActivity, Integer, Boolean> {

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
