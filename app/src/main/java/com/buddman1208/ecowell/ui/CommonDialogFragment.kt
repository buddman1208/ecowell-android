package com.buddman1208.ecowell.ui

import android.os.Bundle
import android.view.View
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseDialogFragment
import com.buddman1208.ecowell.databinding.DialogCommonBinding

class CommonDialogFragment(
    private val title : String = "",
    private val text : String = ""
) : BaseDialogFragment<DialogCommonBinding, CommonDialogViewModel>(
    R.layout.dialog_common
) {

    override val viewModel: CommonDialogViewModel = CommonDialogViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel

        viewModel.dialogContent.set(dialogText)
    }
}

