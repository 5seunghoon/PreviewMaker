package com.tistory.deque.previewmaker.kotlin.util

object EtcConstant{
    const val MAX_SELECT_IMAGE_ACCOUNT = 99

    const val FILE_NAME_FORMAT = "yyyyMMddHHmmssSSS"
    const val FILE_NAME_HEADER_STAMP = "STAMP_"
    const val FILE_NAME_HEADER_PREVIEW = "PREVIEW_"
    const val FILE_NAME_IMAGE_FORMAT = ".png"

    const val MAIN_DIRECTORY = "Pictures"
    const val PREVIEW_SAVED_DIRECTORY = "Preview" + " " + "Maker"
    const val STAMP_SAVED_DIRECTORY = "Stamp"
    const val STAMP_SAVED_DIRECTORY_HIDDEN = ".Stamp"

    const val STAMP_NAME_INTENT_KEY = "STAMP_NAME_INTENT_KEY"
    const val STAMP_ID_INTENT_KEY = "STAMP_ID_INTENT_KEY"
    const val PREVIEW_LIST_INTENT_KEY = "PREVIEW_LIST_INTENT_KEY"

    const val SeekBarStampBrightnessMax = 512
    const val SeekBarPreviewBrightnessMax = 512
    const val SeekBarPreviewContrastMax = 512
    const val SeekBarPreviewSaturationMax = 512
    const val SeekBarPreviewKelvinMax = 512

    const val UCROP_MAX_BITMAP_SIZE = 6000
    const val PREVIEW_BITMAP_SIZE_LIMIT_DEFAULT = 2500
    const val PREVIEW_BITMAP_SIZE_LIMIT_MAX = 5000
    const val STAMP_BITMAP_MAX_SIZE = 1000
    const val STAMP_FILE_MAX_SIZE = 2000
}