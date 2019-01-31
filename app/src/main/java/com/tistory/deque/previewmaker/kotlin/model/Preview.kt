package com.tistory.deque.previewmaker.kotlin.model

import android.net.Uri

data class Preview(
        var originalImageURI: Uri,
        var thumbnailImageURI: Uri,
        var resultImageURI: Uri,
        var isSaved: Boolean,
        

        )