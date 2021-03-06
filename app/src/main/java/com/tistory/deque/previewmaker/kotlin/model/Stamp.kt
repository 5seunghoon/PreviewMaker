package com.tistory.deque.previewmaker.kotlin.model

import android.net.Uri
import com.tistory.deque.previewmaker.kotlin.db.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.model.enums.StampAnchorEnum
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant

data class Stamp(
        var id: Int,
        var imageUri: Uri,
        var name: String,
        var width: Int,
        var height: Int,
        var positionWidthPer: Int,
        var positionHeightPer: Int,
        var positionAnchorEnum: StampAnchorEnum
) {
    constructor(_id: Int, _imageUri: Uri, _name: String, _width: Int,
                _height: Int, _posWidthPer: Int, _posHeightPer: Int, _anchorInt: Int) :
            this(_id, _imageUri, _name, _width, _height, _posWidthPer, _posHeightPer, StampAnchorEnum.valueToEnum(_anchorInt))


    constructor(_id: Int, _imageUri: Uri, _name: String) :
            this(_id, _imageUri, _name, KtDbOpenHelper.STAMP_WIDTH_KEY_INIT_VALUE,
                    KtDbOpenHelper.STAMP_HEIGHT_KEY_INIT_VALUE,
                    KtDbOpenHelper.STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE,
                    KtDbOpenHelper.STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE,
                    StampAnchorEnum.CENTER)

    private var _brightness:Int = 0
    var brightness: Int
        get() {
            //시크바에 들어갈 값이 리턴됨 (0~512)
            //실제 brightness 는 -255~+255
            return _brightness + EtcConstant.SeekBarStampBrightnessMax / 2
        }
        set(value) {
            _brightness = value - EtcConstant.SeekBarStampBrightnessMax / 2
        }

    fun getAbsoluteBrightness(): Int {
        //-255~255값을 반환, 스탬프에 실제로 필터를 적용할때 이용
        return _brightness
    }

    fun resetBrightness() {
        _brightness = 0
    }

}