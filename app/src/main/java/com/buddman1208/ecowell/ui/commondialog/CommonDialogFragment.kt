package com.buddman1208.ecowell.ui.commondialog

import android.os.Bundle
import android.view.View
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.DialogCommonBinding
import com.buddman1208.ecowell.ui.base.BaseDialogFragment

class CommonDialogFragment(
    private val title: String = "",
    private val text: String = "",
    private val _positiveCallback: (() -> Unit)? = null,
    private val _negativeCallback: (() -> Unit)? = null,
    private val _isOnlyConfirmable: Boolean = false,
    _isCancelable: Boolean = true
) : BaseDialogFragment<DialogCommonBinding, CommonDialogViewModel>(
    R.layout.dialog_common
) {


    init {
        isCancelable = _isCancelable
    }

    override val viewModel: CommonDialogViewModel =
        CommonDialogViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        applyData()

    }

    private fun applyData() {
        viewModel.run {
            dialogTitle.set(title)
            dialogContent.set(text)
            isOnlyConfirmable.set(_isOnlyConfirmable)
            positiveCallback = _positiveCallback
            negativeCallback = _negativeCallback
        }
    }
}

