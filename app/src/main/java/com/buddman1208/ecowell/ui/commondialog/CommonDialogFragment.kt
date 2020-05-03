package com.buddman1208.ecowell.ui.commondialog

import android.os.Bundle
import android.view.View
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseDialogFragment
import com.buddman1208.ecowell.databinding.DialogCommonBinding

class CommonDialogFragment(
    private val title: String = "",
    private val text: String = "",
    private val _positiveCallback: (() -> Unit)? = null,
    private val _negativeCallback: (() -> Unit)? = null
) : BaseDialogFragment<DialogCommonBinding, CommonDialogViewModel>(
    R.layout.dialog_common
) {

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
            positiveCallback = _positiveCallback
            negativeCallback = _negativeCallback
        }
    }
}

