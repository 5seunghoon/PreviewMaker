package com.tistory.deque.previewmaker.Model_PreviewData;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.tistory.deque.previewmaker.Util.Logger;
import com.tistory.deque.previewmaker.Activity.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PreviewItem {
    private final static String TAG = "PreviewEditActivity";
    private static int bitmapMaxSize = 2000; // 프리뷰의 비트맵의 장축이 이 사이즈로 제한됨
    private Uri originalImageURI;
    private Uri thumbnailImageURI;
    private Uri resultImageURI;
    //private Bitmap mBitmap; // 생성자에서 받는 originalImageURI에서 비트맵으로 변환하여 바로 저장함
    private Activity mActivity;
    private boolean isSaved;

    public PreviewItem(Uri originalImageURI, Uri thumbnailImageURI, Activity activity) {
        this.originalImageURI = originalImageURI;
        this.thumbnailImageURI = thumbnailImageURI;
        this.mActivity = activity;
        this.isSaved = true;
        this.resultImageURI = makeResultImageFile();
        //mBitmap = URIToBitmap(originalImageURI, mActivity);
    }

    public Bitmap getBitmap() {
        return URIToBitmap(this.originalImageURI, this.mActivity);
    }

    public static Bitmap URIToBitmap(Uri imageUri, Activity activity) {
        Bitmap bitmap;
        Bitmap resizedBitmap = null;
        int width, height;
        double rate;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            rate = (double) width / (double) height;
            if (rate > 1 && width > bitmapMaxSize) { // w > h
                Logger.d(TAG, "RATE : " + rate + " , W : " + bitmapMaxSize + " , H : " + (int) (bitmapMaxSize * (1 / rate)));
                resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmapMaxSize, (int) (bitmapMaxSize * (1 / rate)), true);
            } else if (rate <= 1 && height > bitmapMaxSize) { // h > w
                Logger.d(TAG, "RATE : " + rate + " , W : " + (int) (bitmapMaxSize * (rate)) + " , H : " + bitmapMaxSize);
                resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmapMaxSize * (rate)), bitmapMaxSize, true);
            } else {
                resizedBitmap = bitmap;
            }
            Logger.d(TAG, "URI -> Bitmap success : URI : " + imageUri);
        } catch (FileNotFoundException e) {
            Logger.d(TAG, "URI -> Bitmap : URI File not found" + imageUri);
            e.printStackTrace();
        } catch (IOException e) {
            Logger.d(TAG, "URI -> Bitmap : IOException" + imageUri);
            e.printStackTrace();
        }
        return resizedBitmap;
    }

    public void setOriginalImageURI(Uri originalImageURI) {
        this.originalImageURI = originalImageURI;
        //mBitmap = URIToBitmap(originalImageURI, mActivity);
    }

    private Uri makeResultImageFile() {
        String timeStamp = new SimpleDateFormat(MainActivity.FILE_NAME_FORMAT, Locale.KOREA).format(new Date());
        String imageFileName = MainActivity.FILE_NAME_HEADER_PREVIEW + timeStamp + MainActivity.FILE_NAME_IMAGE_FORMAT;
        File imageFile;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", MainActivity.PREVIEW_SAVED_DIRECTORY);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        imageFile = new File(storageDir, imageFileName);
        Uri resultUri = Uri.fromFile(imageFile);

        Logger.d(TAG, "storageDir : " + storageDir);
        Logger.d(TAG, "image file name : " + imageFileName);
        Logger.d(TAG, "image file uri : " + resultUri);

        return resultUri;
    }

    public static void setBitmapMaxSize(int size) {
        bitmapMaxSize = size;
    }

    public static int getBitmapMaxSize() {
        return bitmapMaxSize;
    }

    public Uri getOriginalImageURI() {
        return originalImageURI;
    }

    public Uri getThumbnailImageURI() {
        return thumbnailImageURI;
    }

    public boolean getIsSaved() {
        return isSaved;
    }

    public void saved() {
        isSaved = true;
    }

    public void editted() {
        isSaved = false;
    }

    public Uri getResultImageURI() {
        return resultImageURI;
    }
}
