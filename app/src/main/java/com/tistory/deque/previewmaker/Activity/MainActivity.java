package com.tistory.deque.previewmaker.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.tistory.deque.previewmaker.Model_Global.DBOpenHelper;
import com.tistory.deque.previewmaker.R;
import com.tistory.deque.previewmaker.Model_StampData.StampAdapter;
import com.tistory.deque.previewmaker.Model_StampData.StampItem;
import com.tistory.deque.previewmaker.Util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity {
    public final static String FILE_NAME_FORMAT = "yyyyMMddHHmmssSSS";
    public final static String FILE_NAME_HEADER_STAMP = "STAMP_";
    public final static String FILE_NAME_HEADER_PREVIEW = "PREVIEW_";
    public final static String FILE_NAME_IMAGE_FORMAT = ".png";

    public final static String MAIN_DIRECTORY = "Pictures";
    public final static String PREVIEW_SAVED_DIRECTORY = "Preview" + " " + "Maker";
    public final static String STAMP_SAVED_DIRECTORY = "Stamp";

    private static int MAX_SELECT_IMAGE_ACCOUNT = 99;

    private final int REQUEST_TAKE_STAMP_FROM_ALBUM = 101;
    private final int REQUEST_MAKE_STAMP_ACTIVITY = 102;
    private final int REQUEST_TAKE_PREVIEW_FROM_ALBUM = 103;
    private final String TAG = "MainActivity";


    @BindView(R.id.mainActivityHintText)
    TextView mMainActivityHintTextView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.recyclerStampView)
    RecyclerView mRecyclerStampView;
    @BindView(R.id.mainActivityMainLayout)
    CoordinatorLayout mainActivityMainLayout;

    private ArrayList<StampItem> mStampItems;
    private StampAdapter mStampAdapter;
    private LinearLayoutManager mRecyclerViewLayoutManager;

    private ArrayList<String> mSeletedPreviews;

    private DBOpenHelper dbOpenHelper;

    private String mCurrentPhotoPath;
    private Uri mCropSourceURI, mCropEndURI; //  mCropSourceURI = 자를 uri, mCropEndURI = 자르고 난뒤 uri

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

        ButterKnife.bind(this);

        setTitle(getString(R.string.app_name));


        dbOpen();


        //setting toolbar

        //permission

        //setting recycler view
        setRecyclerView();

        //floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TedPermission.with(getApplicationContext())
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                getStampFromAlbum();
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                            }
                        })
                        .setRationaleMessage(getString(R.string.tedpermission_add_stamp_rational))
                        .setDeniedMessage(getString(R.string.tedpermission_add_stamp_deny_rational))
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setGotoSettingButton(true)
                        .check();
            }
        });


        //setting navigation view
        //navigationView.setNavigationItemSelectedListener(this);


        stampsFromDBToList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_STAMP_FROM_ALBUM: // 앨범에서 stamp선택 완료
                if (resultCode == Activity.RESULT_OK) {
                    File albumFile = createImageFile();
                    mCropSourceURI = data.getData();
                    Logger.d(TAG, "mCropSourceURI : " + mCropSourceURI);
                    if (checkStampSizeValid(mCropSourceURI)) {
                        mCropEndURI = Uri.fromFile(albumFile);
                        nonCropImage();
                        Logger.d(TAG, "TAKE STAMP FROM ALBUM SUCCESS");
                    }
                } else {
                    Logger.d(TAG, "TAKE STAMP FROM ALBUM FAIL");
                }
                break;

            case REQUEST_MAKE_STAMP_ACTIVITY: // 앨범 이름 등 설정 완료
                if (resultCode == Activity.RESULT_OK) {
                    addStampToListAndDB(data);
                }
                break;

            case REQUEST_TAKE_PREVIEW_FROM_ALBUM: // 앨범에서 preview들 선택 완료
                if (resultCode == Activity.RESULT_OK) {
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

    private boolean checkStampSizeValid(Uri stampBaseUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), stampBaseUri);
            if (bitmap.getHeight() >= 2000 || bitmap.getWidth() >= 2000) {
                Logger.d(TAG, "SIZE OVER");
                Snackbar.make(mainActivityMainLayout, getString(R.string.snackbar_main_acti_stamp_size_over_err), Snackbar.LENGTH_LONG).show();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onBackPressed() {// 드로어가 닫혀있으면 앱 종료
        if (System.currentTimeMillis() - mBackPressedTime > 2000) {
            Snackbar.make(mainActivityMainLayout, getString(R.string.snackbar_main_acti_back_to_exit), Snackbar.LENGTH_LONG)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_help:
                Intent intent1 = new Intent(getApplicationContext(), HelpMainActivity.class);
                startActivity(intent1);
                return true;
            case R.id.action_credit:
                Intent intent2 = new Intent(getApplicationContext(), CreditActivity.class);
                startActivity(intent2);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dbOpen() {
        dbOpenHelper = DBOpenHelper.getDbOpenHelper(
                getApplicationContext()
                , DBOpenHelper.DP_OPEN_HELPER_NAME
                , null
                , DBOpenHelper.dbVersion);
        dbOpenHelper.dbOpen();
    }

    private void setRecyclerView() {
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
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Logger.d(TAG, "getAlbum()");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        Logger.d(TAG, "start Activity : album intent");
        startActivityForResult(intent, REQUEST_TAKE_STAMP_FROM_ALBUM);
    }

    public File createImageFile() {
        Logger.d(TAG, "createImageFile func");
        String timeStamp = new SimpleDateFormat(FILE_NAME_FORMAT, Locale.KOREA).format(new Date());
        String imageFileName = FILE_NAME_HEADER_STAMP + timeStamp + FILE_NAME_IMAGE_FORMAT;
        Logger.d(TAG, "image file name : " + imageFileName
        );
        File imageFile = null;
        File root;
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageParentDir = new File(root, PREVIEW_SAVED_DIRECTORY);
        File storageDir = new File(root + "/" + PREVIEW_SAVED_DIRECTORY, STAMP_SAVED_DIRECTORY);
        Logger.d(TAG, "storageParentDir : " + storageParentDir);
        Logger.d(TAG, "storageDir : " + storageDir);
        if (!storageParentDir.exists()) {
            storageParentDir.mkdirs();
            storageDir.mkdirs();
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Logger.d(TAG, "mCurrentPhotoPath : " + mCurrentPhotoPath);

        return imageFile;
        /*
            root = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File mainDir = new File(root  + MAIN_DIRECTORY);
            if(!mainDir.exists()){
                mainDir.mkdirs();
            }
            File storageParentDir = new File(root  + "/" + MAIN_DIRECTORY, PREVIEW_SAVED_DIRECTORY);
            File storageDir = new File(root  + "/" + MAIN_DIRECTORY + "/" + PREVIEW_SAVED_DIRECTORY, STAMP_SAVED_DIRECTORY);
            Logger.d(TAG, "storageParentDir : " + storageParentDir);
            Logger.d(TAG, "storageDir : " + storageDir);
            if (!storageParentDir.exists()) {
                storageParentDir.mkdirs();
                storageDir.mkdirs();
            }
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            imageFile = new File(storageDir, imageFileName);
            mCurrentPhotoPath = imageFile.getAbsolutePath();
            Logger.d(TAG, "mCurrentPhotoPath : " + mCurrentPhotoPath);

            return imageFile;*/


    }

    public void nonCropImage() {
        /**
         * copy mCropSourceURI and paste to mCropEndURI
         */
        String pathCropSourceURI = getRealPathFromURI(mCropSourceURI);
        File file = new File(pathCropSourceURI);
        Logger.d(TAG, "crop source file size : " + file.length());
        File outFile = new File(mCropEndURI.getPath());
        Logger.d(TAG, "inFile , outFile " + file + " , " + outFile);

        if (file.exists()) {

            try {

                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(outFile);
                int readcount;
                byte[] buffer = new byte[1024];

                while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
                    newfos.write(buffer, 0, readcount);
                }
                newfos.close();
                fis.close();
            } catch (IOException e) {
                Logger.d(TAG, "FILE COPY FAIL");
                Snackbar.make(mainActivityMainLayout, getString(R.string.snackbar_main_acti_stamp_copy_err), Snackbar.LENGTH_LONG);
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

        String[] proj = {MediaStore.Images.Media.DATA};

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

    public static void galleryAddPic(Activity activity, Uri imageURI) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageURI);
        activity.sendBroadcast(mediaScanIntent);
    }

    private void addStampToListAndDB(Intent data) {
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

    public void callFromListItem(int stampPosition) {
        Toast.makeText(getApplicationContext(), getString(R.string.guide_select_previews), Toast.LENGTH_LONG).show();
        getPreviewsFromAlbum(stampPosition);
    }

    public void callFromListItemToDelete(View v, int position) {

        int id = mStampItems.get(position).getID();
        Uri imageURI = mStampItems.get(position).getImageURI();
        String name = mStampItems.get(position).getStampName();
        File file = new File(imageURI.getPath());
        if (!file.exists()) {
            mStampAdapter.notifyDataSetChanged();
            dbOpenHelper.dbDeleteStamp(id);
        } else {
            if (file.delete()) {

                Logger.d(TAG, "Stamp delete suc");
                galleryAddPic(this, imageURI);
                Logger.d(TAG, "media scanning end");

                try {
                    mStampItems.remove(position);
                } catch (IndexOutOfBoundsException e) {
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

    private void getPreviewsFromAlbum(int stampPosition) {
        this.stampPosition = stampPosition;
        mSeletedPreviews = new ArrayList<>();

        Intent intent = new Intent(getApplicationContext(), MultiImageSelectorActivity.class);
        // whether show camera
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
        // max select image amount
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, MAX_SELECT_IMAGE_ACCOUNT);
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
        int id, width, height, posWidthPer, posHeightPer, anchorInt;
        String imageURIPath, name;
        String sql = "SELECT * FROM " + DBOpenHelper.TABLE_NAME_STAMPS + ";";
        Cursor results = dbOpenHelper.db.rawQuery(sql, null);

        Logger.d(TAG, "Cursor open");
        results.moveToFirst();
        while (!results.isAfterLast()) {
            id = results.getInt(0);
            name = results.getString(1);
            imageURIPath = results.getString(2);
            width = results.getInt(3);
            height = results.getInt(4);
            posWidthPer = results.getInt(5);
            posHeightPer = results.getInt(6);
            anchorInt = results.getInt(7);

            Logger.d(TAG, "DB ITEM : id : " + id + " imageURIPath : " + imageURIPath + " name : " + name);

            //if there is not exist stamp file, delete it in db
            File stampFile = new File(Uri.parse(imageURIPath).getPath());
            Logger.d(TAG, " imageURIPath : " + imageURIPath);
            if (!stampFile.exists()) {
                dbOpenHelper.dbDeleteStamp(id);
            } else {
                mStampItems.add(new StampItem(id, Uri.parse(imageURIPath), name, width, height, posWidthPer, posHeightPer, anchorInt));
            }
            results.moveToNext();
        }
        invisibleHint();
    }

    protected void invisibleHint() {
        if (mStampItems.size() > 0) mMainActivityHintTextView.setVisibility(View.GONE);
        Logger.d(TAG, mStampItems.size() + " ->mStampItems size");
    }

    protected void visibleHint() {
        if (mStampItems.size() <= 0) mMainActivityHintTextView.setVisibility(View.VISIBLE);
        Logger.d(TAG, mStampItems.size() + " ->mStampItems size");
    }

}
