package com.tistory.deque.previewmaker.kotlin.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import com.tistory.deque.previewmaker.kotlin.util.SnackbarMessage
import com.tistory.deque.previewmaker.kotlin.util.SnackbarMessageString

open class BaseKotlinViewModel : ViewModel() {

    // 일회성 이벤트를 만들어 내는 라이브 이벤트
    // 뷰는 이러한 이벤트를 바인딩하고 있다가, 적절한 상황이 되면 액티비티를 호출하거나 스낵바를 만듬
    private val snackbarMessage = SnackbarMessage()
    private val snackbarMessageString = SnackbarMessageString()
    private val _startLoadingDialogEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val startLoadingDialogEvent: LiveData<Any> get() = _startLoadingDialogEvent
    private val _stopLoadingDialogEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val stopLoadingDialogEvent: LiveData<Any> get() = _stopLoadingDialogEvent
/*

    */
    /**
     * RxJava 의 observing을 위한 부분.
     * addDisposable을 이용하여 추가하기만 하면 된다
     *//*

    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }
*/

    override fun onCleared() {
        //compositeDisposable.clear()
        super.onCleared()
    }

    /**
     * 스낵바를 보여주고 싶으면 viewModel 에서 이 함수를 호출
     */
    fun showSnackbar(stringResourceId: Int) {
        snackbarMessage.value = stringResourceId
    }

    fun showSnackbar(str: String) {
        snackbarMessageString.value = str
    }

    /**
     * BaseKotlinActivity 에서 쓰는 함수
     */
    fun observeSnackbarMessage(lifeCycleOwner: LifecycleOwner, ob: (Int) -> Unit) {
        snackbarMessage.observe(lifeCycleOwner, ob)
    }

    fun observeSnackbarMessageStr(lifeCycleOwner: LifecycleOwner, ob: (String) -> Unit) {
        snackbarMessageString.observe(lifeCycleOwner, ob)
    }

    fun startLoadingDialog() {
        _startLoadingDialogEvent.call()
    }

    fun stopLoadingDialog() {
        _stopLoadingDialogEvent.call()
    }
}