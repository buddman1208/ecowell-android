package com.buddman1208.ecowell.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ViewDataBinding
import com.tsengvn.typekit.TypekitContextWrapper

abstract class BaseActivity<B : ViewDataBinding, VM : BaseViewModel>(
    @LayoutRes private val layoutResId: Int
) : AppCompatActivity() {


    protected abstract val viewModel: VM

    protected lateinit var binding: B

    private val newActivityCallback by lazy {
        object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val value = viewModel.activityToStart
                value.get()?.run {
                    val intent = Intent(this@BaseActivity, this.first.java)
                    if (this.second != null)
                        intent.putExtras(this.second ?: bundleOf())
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutResId)
        viewModel.activityToStart.addOnPropertyChangedCallback(newActivityCallback)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.activityToStart.removeOnPropertyChangedCallback(newActivityCallback)
    }
}