package com.buddman1208.ecowell.ui

import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseDialogFragment
import com.buddman1208.ecowell.databinding.DialogSettingBinding

class SettingDialogFragment :
    BaseDialogFragment<DialogSettingBinding, SettingDialogViewModel>(
        R.layout.dialog_setting
    ) {

    override val viewModel: SettingDialogViewModel = SettingDialogViewModel()

}