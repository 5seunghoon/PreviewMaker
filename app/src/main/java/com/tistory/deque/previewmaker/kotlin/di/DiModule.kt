package com.tistory.deque.previewmaker.kotlin.di

import com.tistory.deque.previewmaker.kotlin.main.KtMainViewModel
import com.tistory.deque.previewmaker.kotlin.main.KtStampAdapter
import com.tistory.deque.previewmaker.kotlin.makestamp.KtMakeStampViewModel
import com.tistory.deque.previewmaker.kotlin.model.StampAdapterModel
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditViewModel
import com.tistory.deque.previewmaker.kotlin.previewedit.PreviewThumbnailAdapter
import com.tistory.deque.previewmaker.kotlin.setting.KtSettingViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val viewModelPart = module {
    viewModel {
        KtMainViewModel()
    }
    viewModel {
        KtMakeStampViewModel()
    }
    viewModel {
        KtPreviewEditViewModel()
    }
    viewModel {
        KtSettingViewModel()
    }
}

val modelPart = module {
    factory {
        StampAdapterModel()
    }
}

val adapterPart = module {
    factory {
        KtStampAdapter(get())
    }
    factory {
        PreviewThumbnailAdapter()
    }
}

val diModule = listOf(viewModelPart, modelPart, adapterPart)