package com.tistory.deque.previewmaker.Activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tistory.deque.previewmaker.Controler.PreviewBitmapControler;
import com.tistory.deque.previewmaker.Model_Global.ClickState;
import com.tistory.deque.previewmaker.Model_Global.ClickStateEnum;
import com.tistory.deque.previewmaker.Model_Global.DBOpenHelper;
import com.tistory.deque.previewmaker.Model_Global.SeekBarSelectedEnum;
import com.tistory.deque.previewmaker.Model_Global.SeekBarListener;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewEditActivity extends AppCompatActivity {

    private final String TAG = "PreviewEditActivity";

    public static final String EXTRA_STAMP_ID = "STAMP_ID";
    public static final String EXTRA_PREVIEW_LIST = "PREVIEW_LIST";

    public static int canvasGrandParentViewWidth, canvasGrandParentViewHeight;

    protected static int POSITION = -1;
    private boolean isClickPreviewFirst = false;
    private long mBackPressedTime;

    public PreviewBitmapControler pbc;

    private int stampID;
    private Uri stampImageURI;

    private DBOpenHelper dbOpenHelper;
    private StampItem selectedStamp;

    private ArrayList<String> previewPaths;
    private ArrayList<PreviewItem> previewItems;

    @BindView(R.id.previewRecyclerView)
    RecyclerView mRecyclerPreviewView;
    private PreviewAdapter mPreviewAdapter;
    private LinearLayoutManager mRecyclerPreviewViewLayoutManager;

    @BindView(R.id.canvasParentLayout)
    LinearLayout mCanvasPerantLayout;
    @BindView(R.id.canvasGrandParentLayout)
    LinearLayout mCanvasGrandParentLayout;
    private PreviewCanvasView mPreviewCanvasView;

    @BindView(R.id.canvasviewHintTextView)
    TextView canvasviewHintTextView;
    @BindView(R.id.previewLoadingProgressBar)
    ProgressBar previewLoadingProgressBar;
    @BindView(R.id.layoutEditButtonLayout)
    LinearLayout layoutEditButtonLayout;
    @BindView(R.id.layoutStampEditButtonLayout)
    LinearLayout layoutStampEditButtonLayout;
    @BindView(R.id.layoutFilterButtonLayout)
    LinearLayout layoutFilterButtonLayout;

    @BindView(R.id.seekbarParentLinearLayout)
    LinearLayout seekbarParentLinearLayout;
    @BindView(R.id.previewEditAllBtnParentLinearLayout)
    LinearLayout previewEditAllBtnParentLinearLayout;

    private SeekBarListener mSeekBarStampBrightnessListener;
    private SeekBarListener mSeekBarPreviewBrightnessListener;
    private SeekBarListener mSeekBarPreviewContrastListener;
    private SeekBarListener mSeekBarPreviewSaturationListener;
    private SeekBarListener mSeekBarPreviewKelvinListener;


    @BindView(R.id.editSeekBarLayoutDouble)
    LinearLayout editSeekBarLayoutDouble;

    @BindView(R.id.editSeekBar1)
    SeekBar editSeekbar1;
    @BindView(R.id.editSeekBarLayout1)
    LinearLayout editSeekBarLayout1;
    @BindView(R.id.seekBarTextViewLeft1)
    TextView seekBarTextViewLeft1;
    @BindView(R.id.seekBarTextViewRight1)
    TextView seekBarTextViewRight1;

    @BindView(R.id.editSeekBar2)
    SeekBar editSeekbar2;
    @BindView(R.id.editSeekBarLayout2)
    LinearLayout editSeekBarLayout2;
    @BindView(R.id.seekBarTextViewLeft2)
    TextView seekBarTextViewLeft2;
    @BindView(R.id.seekBarTextViewRight2)
    TextView seekBarTextViewRight2;

    @BindView(R.id.editSeekBarSingle)
    SeekBar editSeekbarSingle;
    @BindView(R.id.editSeekBarLayoutSingle)
    LinearLayout editSeekBarLayoutSingle;
    @BindView(R.id.seekBarTextViewLeftSingle)
    TextView seekBarTextViewLeftSingle;
    @BindView(R.id.seekBarTextViewRightSingle)
    TextView seekBarTextViewRightSingle;

    public PreviewCanvasView getmPreviewCanvasView() {
        return mPreviewCanvasView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_edit);

        ButterKnife.bind(this);

        setTitle(R.string.title_preview_make_activity);


        //뒤로가기 버튼
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        POSITION = -1;
        isClickPreviewFirst = false;

        Intent intent = getIntent();
        stampID = intent.getExtras().getInt(EXTRA_STAMP_ID);
        stampImageURI = intent.getData();
        previewPaths = new ArrayList<>();
        previewItems = new ArrayList<>();
        previewPaths = intent.getStringArrayListExtra(EXTRA_PREVIEW_LIST);

        pbc = PreviewBitmapControler.getPreviewBitmapControler(this);

        setRecyclerView();
        setPreviewCanvas();
        setSeekBar();
        setStamp(stampID);

        setVisibleInit();


        LoadingPreviewThumbnail loadingPreviewThumbnail = new LoadingPreviewThumbnail();
        loadingPreviewThumbnail.execute(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //뒤로가기 버튼
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        switch (requestCode) {
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

                    mPreviewCanvasView.cropPreview();

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

        try {
            selectedStamp = stampsFromDB(stampID);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void stampUpdate(int id, int width, int height, int posWidthPer, int posHeightPer, int anchorInt) {
        dbOpenHelper.dbUpdateStamp(id, width, height, posWidthPer, posHeightPer, anchorInt);
    }

    private StampItem stampsFromDB(int stampID) throws FileNotFoundException {
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

    private void setSeekBar() {
        mSeekBarStampBrightnessListener = new SeekBarListener(this, SeekBarSelectedEnum.BRIGHTNESS, mPreviewCanvasView);
        mSeekBarPreviewBrightnessListener = new SeekBarListener(this, SeekBarSelectedEnum.PREVIEW_BRIGHTNESS, mPreviewCanvasView);
        mSeekBarPreviewContrastListener = new SeekBarListener(this, SeekBarSelectedEnum.PREVIEW_CONTRAST, mPreviewCanvasView);
        mSeekBarPreviewSaturationListener = new SeekBarListener(this, SeekBarSelectedEnum.PREVIEW_SATURATION, mPreviewCanvasView);
        mSeekBarPreviewKelvinListener = new SeekBarListener(this, SeekBarSelectedEnum.PREVIEW_KELVIN, mPreviewCanvasView);

        editSeekbarSingle.setMax(SeekBarListener.SeekBarStampBrightnessMax);
        editSeekbar1.setMax(SeekBarListener.SeekBarPreviewSaturationMax);
        editSeekbar2.setMax(SeekBarListener.SeekBarPreviewSaturationMax);
        //editSeekbar1.setProgress(SeekBarListener.SeekBarStampBrightnessMax / 2);

    }

    private void setVisibleInit() {
        seekbarInvisibleAllBtnVisible();

        //프리뷰 보여질 캔버스와 힌트 텍스트
        mCanvasPerantLayout.setVisibility(View.INVISIBLE);
        canvasviewHintTextView.setVisibility(View.VISIBLE);

        //버튼 레이아웃들
        layoutStampEditButtonLayout.setVisibility(View.INVISIBLE);
        layoutEditButtonLayout.setVisibility(View.VISIBLE);
        layoutFilterButtonLayout.setVisibility(View.INVISIBLE);

        //시크바 레이아웃
        //allSeekbarInvisible();

    }

    private void seekbarVisibleAllBtnInvisible(){
        seekbarParentLinearLayout.setVisibility(View.VISIBLE);
        previewEditAllBtnParentLinearLayout.setVisibility(View.INVISIBLE);
    }
    private void seekbarInvisibleAllBtnVisible(){
        seekbarParentLinearLayout.setVisibility(View.INVISIBLE);
        previewEditAllBtnParentLinearLayout.setVisibility(View.VISIBLE);
    }

    private void visibleDoubleSeekbar(){
        editSeekBarLayoutDouble.setVisibility(View.VISIBLE);
        editSeekBarLayoutSingle.setVisibility(View.INVISIBLE);
        seekbarVisibleAllBtnInvisible();
    }

    private void visibleSingleSeekbar(){
        editSeekBarLayoutDouble.setVisibility(View.INVISIBLE);
        editSeekBarLayoutSingle.setVisibility(View.VISIBLE);
        seekbarVisibleAllBtnInvisible();
    }

    @OnClick(R.id.buttonSaveEach)
    public void clickButtonSaveEach() {
        mPreviewCanvasView.savePreviewEach(-1, false);
    }

    @OnClick(R.id.buttonCrop)
    public void clickButtonCrop() {
        if (POSITION < 0 || POSITION >= previewItems.size()) {
            return;
        }
        Logger.d(TAG, "Click crop button");
        Uri destURI = previewItems.get(POSITION).getResultImageURI();
        Uri origURI = previewItems.get(POSITION).getOriginalImageURI();

        UCrop.Options options = setCropViewOption();
        UCrop.of(origURI, destURI)
                .withOptions(options)
                .start(this);

        Logger.d(TAG, "crop start : orig : " + origURI + ", dest : " + destURI);
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

    @OnClick(R.id.buttonStamp)
    public void clickButtonStamp() {
        doClickButtonStamp();
    }

    public void doClickButtonStamp(){
        if (POSITION < 0) return;

        if (!mPreviewCanvasView.isStampShown()) {
            mPreviewCanvasView.showStamp();
            if (selectedStamp != null) {
                mPreviewCanvasView.setStampItem(selectedStamp);
            }
            mPreviewCanvasView.callInvalidate();
        } else {
            mPreviewCanvasView.clickStamp();
        }
    }

    @OnClick(R.id.buttonDelete)
    public void clickButtonDelete() {
        //allSeekbarInvisible();
        seekbarInvisibleAllBtnVisible();

        if (previewItems.size() == 1) return;

        int removePos = POSITION;

        mPreviewAdapter.notifyItemRemoved(removePos);
        mPreviewAdapter.notifyItemRangeChanged(0, previewItems.size());

        if (POSITION == previewItems.size() - 1) { // end of list
            POSITION = 0;
        }
        previewItems.remove(removePos);
        previewPaths.remove(removePos);

        mPreviewCanvasView.changeAndInitPreviewInCanvas(POSITION, false);
    }

    @OnClick(R.id.buttonFilter)
    public void clickButtonFilter(){
        if (POSITION < 0) return;

        previewItems.get(POSITION).editted();

        mPreviewCanvasView.clickFilterEditStart();
    }

    @OnClick(R.id.buttonPreviewBrightnessContrast)
    public void clickButtonPreviewBrightnessContrast(){
        seekBarTextViewLeft1.setText(getString(R.string.action_preview_edit_brightness_title));
        seekBarTextViewLeft2.setText(getString(R.string.action_preview_edit_contrast_title));

        editSeekbar1.setOnSeekBarChangeListener(mSeekBarPreviewBrightnessListener);
        editSeekbar1.setMax(SeekBarListener.SeekBarPreviewBrightnessMax);
        editSeekbar1.setProgress(previewItems.get(POSITION).getBrightness());
        setStampSeekBarText(previewItems.get(POSITION).getBrightness(), SeekBarSelectedEnum.PREVIEW_BRIGHTNESS);

        editSeekbar2.setOnSeekBarChangeListener(mSeekBarPreviewContrastListener);
        editSeekbar2.setMax(SeekBarListener.SeekBarPreviewContrastMax);
        editSeekbar2.setProgress(previewItems.get(POSITION).getContrast());
        setStampSeekBarText(previewItems.get(POSITION).getContrast(), SeekBarSelectedEnum.PREVIEW_CONTRAST);

        //editSeekBarLayoutDouble.setVisibility(View.VISIBLE);
        visibleDoubleSeekbar();
        mPreviewCanvasView.callInvalidate();
    }


    @OnClick(R.id.buttonPreviewSaturationKelvin)
    public void clickButtonPreviewSaturationKelvin(){
        seekBarTextViewLeft1.setText(getString(R.string.action_preview_edit_saturation_title));
        seekBarTextViewLeft2.setText(getString(R.string.action_preview_edit_kelvin_title));


        editSeekbar1.setOnSeekBarChangeListener(mSeekBarPreviewSaturationListener);
        editSeekbar1.setMax(SeekBarListener.SeekBarPreviewSaturationMax);
        editSeekbar1.setProgress(previewItems.get(POSITION).getSaturation());
        setStampSeekBarText(previewItems.get(POSITION).getSaturation(), SeekBarSelectedEnum.PREVIEW_SATURATION);


        editSeekbar2.setOnSeekBarChangeListener(mSeekBarPreviewKelvinListener);
        editSeekbar2.setMax(SeekBarListener.SeekBarPreviewSaturationMax);
        editSeekbar2.setProgress(previewItems.get(POSITION).getKelvin());
        setStampSeekBarText(previewItems.get(POSITION).getKelvin(), SeekBarSelectedEnum.PREVIEW_SATURATION);


        //editSeekBarLayoutDouble.setVisibility(View.VISIBLE);
        visibleDoubleSeekbar();
        mPreviewCanvasView.callInvalidate();
    }

    @OnClick(R.id.buttonPreviewBlur)
    public void clickButtonPreivewBlur(){

    }

    @OnClick(R.id.buttonFilterReset)
    public void clickButtonFilterReset(){
        //allSeekbarInvisible();
        seekbarInvisibleAllBtnVisible();
        previewItems.get(POSITION).resetFilterValue();
        mPreviewCanvasView.callInvalidate();
    }


    @OnClick(R.id.buttonPreviewEditFinish)
    public void clickButtonPreviewEditFinish(){
        //editSeekBarLayout1.setVisibility(View.INVISIBLE);
        //allSeekbarInvisible();
        seekbarInvisibleAllBtnVisible();
        mPreviewCanvasView.finishPreviewEdit();
        mPreviewCanvasView.savePreviewEach(-1, true);
    }

    @OnClick(R.id.buttonStampFinish)
    public void clickButtonStampFinish() {
        //editSeekBarLayout1.setVisibility(View.INVISIBLE);
        //allSeekbarInvisible();
        seekbarInvisibleAllBtnVisible();
        mPreviewCanvasView.finishStampEdit();
    }

    @OnClick(R.id.buttonStampDelete)
    public void clickButtonStampDelete() {
        //editSeekBarLayout1.setVisibility(View.INVISIBLE);
        //allSeekbarInvisible();
        seekbarInvisibleAllBtnVisible();
        mPreviewCanvasView.deleteStamp();
    }

    @OnClick(R.id.buttonStampBrightness)
    public void clickButtonStampBrightness() {
        /**
         * 할일 : 시크바의 max변경, 비저빌리티 변경, 프로그레스값 변경, 리스너 변경, 텍스트 변경
         */
        seekBarTextViewLeftSingle.setText(getString(R.string.action_stamp_edit_brightness_title_short));
        editSeekbarSingle.setOnSeekBarChangeListener(mSeekBarStampBrightnessListener);
        editSeekbarSingle.setMax(SeekBarListener.SeekBarStampBrightnessMax);
        //editSeekBarLayoutSingle.setVisibility(View.VISIBLE);
        editSeekbarSingle.setProgress(selectedStamp.getBrightness());
        setStampSeekBarText(selectedStamp.getBrightness(), SeekBarSelectedEnum.BRIGHTNESS);

        visibleSingleSeekbar();
        mPreviewCanvasView.callInvalidate();
    }

    @OnClick(R.id.buttonStampReset)
    public void clickButtonStampReset() {
        //editSeekBarLayout1.setVisibility(View.INVISIBLE);
        //allSeekbarInvisible();
        seekbarInvisibleAllBtnVisible();
        mPreviewCanvasView.stampReset();
    }

    @OnClick(R.id.buttonPreviewEditOK)
    public void clickButtonPreviewEditOK(){
        seekbarInvisibleAllBtnVisible();
    }


    public void setStampSeekBarText(int value, SeekBarSelectedEnum selected) {
        int resultProgressValue;
        switch (selected){
            case BRIGHTNESS:
                resultProgressValue = (int) ((value - SeekBarListener.SeekBarStampBrightnessMax / 2f) / (SeekBarListener.SeekBarStampBrightnessMax / 2f) * 100f);
                seekBarTextViewRightSingle.setText(resultProgressValue + "%");
                break;
            case CONTRAST:
                break;
            case PREVIEW_BRIGHTNESS:
                resultProgressValue = (int) ((value - SeekBarListener.SeekBarPreviewBrightnessMax / 2f) / (SeekBarListener.SeekBarPreviewBrightnessMax / 2f) * 100f);
                seekBarTextViewRight1.setText(resultProgressValue + "%");
                break;
            case PREVIEW_CONTRAST:
                resultProgressValue = (int) ((value - SeekBarListener.SeekBarPreviewContrastMax / 2f) / (SeekBarListener.SeekBarPreviewContrastMax / 2f) * 100f);
                seekBarTextViewRight2.setText(resultProgressValue + "%");
                break;
            case PREVIEW_SATURATION:
                resultProgressValue = (int) ((value - SeekBarListener.SeekBarPreviewSaturationMax / 2f) / (SeekBarListener.SeekBarPreviewSaturationMax / 2f) * 100f);
                seekBarTextViewRight1.setText(resultProgressValue + "%");
                break;
            case PREVIEW_KELVIN:
                resultProgressValue = (int) ((value - SeekBarListener.SeekBarPreviewKelvinMax / 2f) / (SeekBarListener.SeekBarPreviewKelvinMax / 2f) * 100f);
                seekBarTextViewRight2.setText(resultProgressValue + "%");
                break;
        }
    }

    private void setPreviewCanvas() {
        mPreviewCanvasView = new PreviewCanvasView(this, this, previewItems, pbc);
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
        mRecyclerPreviewView.setHasFixedSize(true);

        mRecyclerPreviewViewLayoutManager = new LinearLayoutManager(this);
        mRecyclerPreviewViewLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerPreviewView.setLayoutManager(mRecyclerPreviewViewLayoutManager);
        mRecyclerPreviewView.setItemAnimator(new DefaultItemAnimator());

        mPreviewAdapter = new PreviewAdapter(this, previewItems);
        mRecyclerPreviewView.setAdapter(mPreviewAdapter);
    }

    public void clickPreviewItem(int position) {
        if (mPreviewCanvasView.isNowEditingStamp()) return;

        if (POSITION != position) {
            mPreviewCanvasView.clickNewPreview(position);
        }

        if (!isClickPreviewFirst) {
            isClickPreviewFirst = true;
            canvasviewHintTextView.setVisibility(View.INVISIBLE);
            mCanvasPerantLayout.setVisibility(View.VISIBLE);
            mPreviewCanvasView.changeAndInitPreviewInCanvas(position, false);
        }
    }

    public void editButtonInvisibleOrVisible(ClickState CLICK_STATE) {
        if (CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_STAMP_EDIT ||
                CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_STAMP_ZOOM) {
            layoutEditButtonLayout.setVisibility(View.INVISIBLE);
            layoutStampEditButtonLayout.setVisibility(View.VISIBLE);
            layoutFilterButtonLayout.setVisibility(View.INVISIBLE);
        } else if(CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_NONE_CLICK){
            layoutEditButtonLayout.setVisibility(View.VISIBLE);
            layoutStampEditButtonLayout.setVisibility(View.INVISIBLE);
            layoutFilterButtonLayout.setVisibility(View.INVISIBLE);
        } else if(CLICK_STATE.getClickStateEnum() == ClickStateEnum.STATE_BITMAP_FILTER){
            layoutEditButtonLayout.setVisibility(View.INVISIBLE);
            layoutStampEditButtonLayout.setVisibility(View.INVISIBLE);
            layoutFilterButtonLayout.setVisibility(View.VISIBLE);
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
        return imageIdToThumbnail("" + rowId);
    }

    public Uri imageIdToThumbnail(String imageId) {
        String[] projection = {MediaStore.Images.Thumbnails.DATA};
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
        while (!results.isAfterLast()) {
            _id = results.getInt(0);
            _name = results.getString(1);
            _imageURI = results.getString(2);
            Logger.d(TAG, "DB ITEM : id : " + _id + " imageURI : " + _imageURI + " name : " + _name +
                    " W : " + results.getString(3) + " H : " + results.getString(4) + " W P : " + results.getString(5) + " H P : " + results.getString(6));
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
            boolean firstAdd = true;

            for (String previewPath : previewPaths) { // previewPaths -> previewItems
                thumbnailUri = thumbnailURIFromOriginalURI(previewPath);
                originalUri = getUriFromPath(previewPath);
                if (thumbnailUri == null) {
                    thumbnailUri = originalUri;
                    Logger.d(TAG, "Thumbnail parsing error");
                }
                Logger.d(TAG, "Thumbnail parsing success : " + thumbnailUri);
                previewItems.add(new PreviewItem(originalUri, thumbnailUri, param[0]));
                if(firstAdd) {
                    pbc.setPreviewBitmap(previewItems.get(0));
                    firstAdd = false;
                }
                Logger.d(TAG, "previewItem success : " + originalUri);

                loadingCounter++;
                publishProgress();
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { // 중간 업데이트
            super.onProgressUpdate(values);
            mPreviewAdapter.notifyItemInserted((int) loadingCounter);
            double size = previewPaths.size();
            double progress = loadingCounter / size;
            previewLoadingProgressBar.setProgress((int) (100.0 * progress));
        }

        @Override
        protected void onPostExecute(Boolean result) { // 실행 완료
            super.onPostExecute(result);
            previewLoadingProgressBar.setVisibility(View.GONE);
        }
    }

}
