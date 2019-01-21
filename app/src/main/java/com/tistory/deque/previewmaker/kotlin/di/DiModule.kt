package com.tistory.deque.previewmaker.kotlin.di

import com.tistory.deque.previewmaker.kotlin.main.KtMainViewModel
import com.tistory.deque.previewmaker.kotlin.main.KtStampAdapter
import com.tistory.deque.previewmaker.kotlin.model.StampAdapterModel
import org.koin.dsl.module.module

val viewModelPart = module {
    KtMainViewModel()
}

val modelPart = module {
    StampAdapterModel()
}

val adapterPart = module {
    KtStampAdapter(get())
}

val diModule = listOf(viewModelPart)