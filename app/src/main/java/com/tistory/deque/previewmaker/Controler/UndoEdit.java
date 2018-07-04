package com.tistory.deque.previewmaker.Controler;

import java.util.Map;

public class UndoEdit {
    public static boolean hasUndo = false;

    public static float previewBrightness = 0f;
    public static float previewContrast = 0f;
    public static float previewSaturation = 0f;
    public static float previewKelvin = 0f;

    public static String hasUndoKey = "HAS_UNDO";
    public static String previewBrightnessKey = "PREVIEW_BRIGHTNESS_KEY";
    public static String previewContrastKey = "PREVIEW_CONTRAST_KEY";
    public static String previewSaturationKey = "PREVIEW_SATURATION_KEY";
    public static String previewKelvinKey = "PREVIEW_KELVIN_KEY";

    public static void setUndo(float brightness, float contrast, float saturation, float kelvin){
        previewBrightness = brightness;
        previewContrast = contrast;
        previewSaturation = saturation;
        previewKelvin = kelvin;
        hasUndo = true;
    }

    public static Map<String, Float> getUndo(){
        Map<String, Float> result = null;
        result.put(previewBrightnessKey, previewBrightness);
        result.put(previewContrastKey, previewContrast);
        result.put(previewSaturationKey, previewSaturation);
        result.put(previewKelvinKey, previewKelvin);
        return result;
    }
}
