package com.tistory.deque.previewmaker.Model_PreviewData;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.tistory.deque.previewmaker.Model_Global.SeekBarListener;
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

    private int brightness;
    private int contrast;
    private int kelvin;
    private int saturation;

    public PreviewItem(Uri originalImageURI, Uri thumbnailImageURI, Activity activity) {
        this.originalImageURI = originalImageURI;
        this.thumbnailImageURI = thumbnailImageURI;
        this.mActivity = activity;
        this.isSaved = true;
        this.resultImageURI = makeResultImageFile();

        this.brightness = 0;
        this.contrast = 0;
        this.kelvin = 0;
        this.saturation = 0;
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


        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API LEVEL이 26보다 클때 (8.0이상)
        //if (true) {
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File storageDir = new File(root, MainActivity.PREVIEW_SAVED_DIRECTORY);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            imageFile = new File(storageDir, imageFileName);
            Uri resultUri = Uri.fromFile(imageFile);

            Logger.d(TAG, "storageDir : " + storageDir);
            Logger.d(TAG, "image file name : " + imageFileName);
            Logger.d(TAG, "image file uri : " + resultUri);

            return resultUri;
       /*
            File mainDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
            if (!mainDir.exists()) {
                mainDir.mkdirs();
            }
            File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", MainActivity.PREVIEW_SAVED_DIRECTORY);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            imageFile = new File(storageDir, imageFileName);
            Uri resultUri = Uri.fromFile(imageFile);

            Logger.d(TAG, "storageDir : " + storageDir);
            Logger.d(TAG, "image file name : " + imageFileName);
            Logger.d(TAG, "image file uri : " + resultUri);

            return resultUri;
        */

    }

    public int getBrightness() {
        //시크바에 들어갈 값이 리턴됨 (0~512)
        //실제 brightness 는 -255~+255
        return brightness + SeekBarListener.SeekBarPreviewBrightnessMax / 2;
    }

    public float getBrightnessForFilter() {
        //실제 필터를 적용할 때 이용. -256~+256을 -64 ~ +64로 바꿔서 리턴
        return brightness / 4.0f;
    }

    public void setBrightness(int brightness) {
        //0~512를 인자로 받아서 -255~+255로 수정후 저장
        this.brightness = brightness - SeekBarListener.SeekBarPreviewBrightnessMax / 2;
    }

    public int getContrast() {
        //시크바에 들어갈 값이 리턴됨 (0~512)
        //실제 brightness 는 -255~+255
        return contrast + SeekBarListener.SeekBarPreviewContrastMax / 2;
    }

    public float getContrastForFilter() {
        //실제 필터를 적용할 때 이용. 0.5~1.5를 리턴
        return ((float) contrast / 512.0f) + 1.0f;
    }

    public void setContrast(int contrast) {
        //0~512를 인자로 받아서 -255~+255로 수정후 저장
        this.contrast = contrast - SeekBarListener.SeekBarPreviewContrastMax / 2;
    }

    public int getSaturation() {
        return saturation + SeekBarListener.SeekBarPreviewSaturationMax / 2;
    }

    public float getSaturationForFilter() {
        //0.875~1.125를 리턴
        return ((float) saturation / 2048.0f) + 1.0f;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation - SeekBarListener.SeekBarPreviewSaturationMax / 2;
    }

    public int getKelvin() {
        return kelvin + SeekBarListener.SeekBarPreviewKelvinMax / 2;
    }

    public float getKelvinForFilter() {
        //0.875~1.125를 리턴
        return ((float) kelvin / 1024.0f) + 1.0f;
    }

    public void setKelvin(int kelvin) {
        this.kelvin = kelvin - SeekBarListener.SeekBarPreviewKelvinMax / 2;
    }

    public void resetFilterValue() {
        this.brightness = 0;
        this.contrast = 0;
        this.kelvin = 0;
        this.saturation = 0;
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
