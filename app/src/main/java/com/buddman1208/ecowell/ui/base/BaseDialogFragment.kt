package com.buddman1208.ecowell.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment<B : ViewDataBinding, VM : BaseDialogViewModel>(
    @LayoutRes private val layoutResId: Int,
    private val width: Int = ViewGroup.LayoutParams.MATCH_PARENT
) : DialogFragment() {

    abstract val viewModel: VM
    protected lateinit var binding: B

    private val dismissCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val event = viewModel.dismissEvent.get()
            if (event != null && event.isNotEmpty()) dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dismissEvent.addOnPropertyChangedCallback(dismissCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.dismissEvent.removeOnPropertyChangedCallback(dismissCallback)
    }

}