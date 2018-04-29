package com.tistory.deque.previewmaker;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class PreviewEditActivity extends AppCompatActivity {
  private final String TAG = "PreviewEditActivity";
  public static final String URI_ERROR = "URI_ERROR";
  public static final String EXTRA_STAMP_ID = "STAMP_ID";
  public static final String EXTRA_PREVIEW_LIST = "PREVIEW_LIST";

  int stampID;
  Uri stampImageURI;

  ArrayList<String> previewPaths;
  ArrayList<PreviewItem> previewItems;
  RecyclerView mRecyclerPreviewView;
  PreviewAdapter mPreviewAdapter;
  LinearLayoutManager mRecyclerPreviewViewLayoutManager;

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

    String thumbnailUriString;
    for (String previewPath : previewPaths) {
      thumbnailUriString = thumbnailURIFromOriginalURI(Uri.parse(previewPath));
      if(thumbnailUriString == URI_ERROR){
        thumbnailUriString = previewPath;
        Toast.makeText(getApplicationContext(), "썸네일 파싱 에러", Toast.LENGTH_LONG).show();
      }
      previewItems.add(new PreviewItem(Uri.parse(previewPath), Uri.parse(thumbnailUriString)));
      Logger.d(TAG, previewPath);
    }

    setTitle(R.string.title_preview_make_activity);

    setRecyclerView();
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
    Snackbar.make(v, "POSITION : " + position, Snackbar.LENGTH_LONG).show();
  }

  public String thumbnailURIFromOriginalURI(Uri originalImageURI){
    String imageId = originalImageURI.getPath();

    // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
    String[] projection = { MediaStore.Images.Thumbnails.DATA };
    ContentResolver contentResolver = getApplicationContext().getContentResolver();

    // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
    Cursor thumbnailCursor = contentResolver.query(
      MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 썸네일 컨텐트 테이블
      projection, // DATA를 출력
      MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // IMAGE_ID는 원본 이미지의 _ID를 나타냅니다.
      new String[]{imageId},
      null);
    if (thumbnailCursor == null) {
      return URI_ERROR;
    } else if (thumbnailCursor.moveToFirst()) {
      int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);

      String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
      thumbnailCursor.close();
      return thumbnailPath;
    } else {
      // thumbnailCursor가 비었습니다.
      // 이는 이미지 파일이 있더라도 썸네일이 존재하지 않을 수 있기 때문입니다.
      // 보통 이미지가 생성된 지 얼마 되지 않았을 때 그렇습니다.
      // 썸네일이 존재하지 않을 때에는 아래와 같이 썸네일을 생성하도록 요청합니다
      MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
      thumbnailCursor.close();
      return thumbnailURIFromOriginalURI(Uri.parse(imageId));
    }
  }
}
