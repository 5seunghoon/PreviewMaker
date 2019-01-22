package com.tistory.deque.previewmaker.kotlin.di

import com.tistory.deque.previewmaker.kotlin.main.KtMainViewModel
import com.tistory.deque.previewmaker.kotlin.main.KtStampAdapter
import com.tistory.deque.previewmaker.kotlin.model.StampAdapterModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val viewModelPart = module {
    viewModel {
        KtMainViewModel()
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
}

val diModule = listOf(viewModelPart, modelPart, adapterPart)