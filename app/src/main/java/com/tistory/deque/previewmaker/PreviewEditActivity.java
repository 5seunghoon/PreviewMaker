package com.tistory.deque.previewmaker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class PreviewEditActivity extends AppCompatActivity {
  private final String TAG = "PreviewEditActivity";
  public static final String URI_ERROR = "URI_ERROR";
  public static final String EXTRA_STAMP_ID = "STAMP_ID";
  public static final String EXTRA_PREVIEW_LIST = "PREVIEW_LIST";

  protected static int POSITION = -1;

  int stampID;
  Uri stampImageURI;

  ArrayList<String> previewPaths;
  ArrayList<PreviewItem> previewItems;
  private RecyclerView mRecyclerPreviewView;
  private PreviewAdapter mPreviewAdapter;
  private LinearLayoutManager mRecyclerPreviewViewLayoutManager;

  private LinearLayout mCanvasPerantLayout;
  private PreviewCanvasView mPreviewCanvasView;

  StampItem stamp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preview_edit);
    Intent intent = getIntent();
    stampID = intent.getExtras().getInt("STAMP_ID");
    stampImageURI = intent.getData();
    previewPaths = new ArrayList<>();
    previewItems = new ArrayList<>();
    previewPaths = intent.getStringArrayListExtra(EXTRA_PREVIEW_LIST);

    Uri thumbnailUri, originalUri;
    for (String previewPath : previewPaths) { // previewPaths -> previewItems
      thumbnailUri = thumbnailURIFromOriginalURI(previewPath);
      originalUri = getUriFromPath(previewPath);
      if(thumbnailUri == null){
        thumbnailUri = originalUri;
        Logger.d(TAG, "Thumbnail parsing error");
      }
      Logger.d(TAG, "Thumbnail parsing success : " + thumbnailUri);
      previewItems.add(new PreviewItem(originalUri, thumbnailUri, this));
      Logger.d(TAG, "previewItem success : " + originalUri);
    }

    setTitle(R.string.title_preview_make_activity);

    setRecyclerView();

    setPreviewCanvas();

  }

  private void setPreviewCanvas(){
    mCanvasPerantLayout = findViewById(R.id.canvasParentLayout);
    mPreviewCanvasView = new PreviewCanvasView(this, this, previewItems);
    mCanvasPerantLayout.addView(mPreviewCanvasView);
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
    //Snackbar.make(v, "POSITION : " + position, Snackbar.LENGTH_LONG).show();
    POSITION = position;
    mPreviewCanvasView.callInvalidate();
  }

  protected void clickCropButton(){
    /**
     * 리스트에서 크롭을 클릭하면
     * 1. 크롭 액티비티 호출해서 편집 후
     * 2. 기존 이미지 삭제 및 편집한 사진 저장하고
     * 3. 아이템 리스트에서 uri를 다시 세팅해준다음
     * 4. 전역변수 position을 변경해주고
     * 5. 캔버스의 invalidate를 호출
     */
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
    Uri selectedImageUri = getUriFromPath(path);
    long rowId = Long.valueOf(selectedImageUri.getLastPathSegment());
    Logger.d(TAG, "original uri : " + selectedImageUri + " , row ID : " + rowId);
    return uriToThumbnail(""+ rowId);
  }

  public Uri uriToThumbnail(String imageId) {
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
      return uriToThumbnail(imageId);
    }
  }
}
