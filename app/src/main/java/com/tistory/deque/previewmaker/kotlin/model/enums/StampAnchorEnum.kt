package com.tistory.deque.previewmaker.kotlin.model.enums

import java.io.Serializable

enum class StampAnchorEnum(val value:Int) {
    LEFT_TOP(0),
    TOP(1),
    RIGHT_TOP(2),
    LEFT_CENTER(3),
    CENTER(4),
    RIGHT_CENTER(5),
    LEFT_BOTTOM(6),
    BOTTOM(7),
    RIGHT_BOTTOM(8);

    companion object {
        fun valueToEnum(v:Int): StampAnchorEnum{
            StampAnchorEnum.values().forEach {
                if(it.value == v) return it
            }
            return LEFT_TOP
        }
    }

}