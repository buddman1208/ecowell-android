package com.buddman1208.ecowell.ui.base

import android.os.Bundle
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlin.reflect.KClass


abstract class BaseViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val activityToStart = ObservableField<Pair<KClass<*>, Bundle?>>()
    val dialogToStart = ObservableField<Pair<KClass<*>, Bundle?>>()
    val browseToStart = ObservableField<String>()


    protected val mutableErrorTextResId = MutableLiveData<Int>()
    val errorTextResId: LiveData<Int> get() = mutableErrorTextResId

    protected fun Disposable.addDisposable() {
        compositeDisposable.add(this)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}

