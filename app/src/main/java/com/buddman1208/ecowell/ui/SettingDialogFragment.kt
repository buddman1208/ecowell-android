package com.buddman1208.ecowell.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseDialogFragment
import com.buddman1208.ecowell.databinding.DialogSettingBinding

class SettingDialogFragment :
    BaseDialogFragment<DialogSettingBinding, SettingDialogViewModel>(
        R.layout.dialog_setting
    ) {

    override val viewModel: SettingDialogViewModel = SettingDialogViewModel()

    private val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val value = viewModel.saveAction.get()
            if (value != null && value.isNotBlank()) {
                val dialog = CommonDialogFragment(
                    String.format(resources.getString(R.string.ask_save), value)
                )
                dialog.show(requireActivity().supportFragmentManager, "")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        viewModel.saveAction.addOnPropertyChangedCallback(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.saveAction.removeOnPropertyChangedCallback(callback)
    }

}