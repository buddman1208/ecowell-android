package com.buddman1208.ecowell.ui

import android.os.Bundle
import android.view.View
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseDialogFragment
import com.buddman1208.ecowell.databinding.DialogSettingBinding

class SettingDialogFragment :
    BaseDialogFragment<DialogSettingBinding, SettingDialogViewModel>(
        R.layout.dialog_setting
    ) {

    override val viewModel: SettingDialogViewModel = SettingDialogViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
    }

}