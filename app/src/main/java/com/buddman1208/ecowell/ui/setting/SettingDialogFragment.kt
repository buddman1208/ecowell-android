package com.buddman1208.ecowell.ui.setting

import android.os.Bundle
import android.view.View
import androidx.core.os.ConfigurationCompat
import androidx.databinding.Observable
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.DialogSettingBinding
import com.buddman1208.ecowell.ui.base.BaseDialogFragment
import com.buddman1208.ecowell.ui.commondialog.CommonDialogFragment
import com.buddman1208.ecowell.ui.main.MainActivity
import com.buddman1208.ecowell.utils.CredentialManager
import com.buddman1208.ecowell.utils.SettingCache

class SettingDialogFragment :
    BaseDialogFragment<DialogSettingBinding, SettingDialogViewModel>(
        R.layout.dialog_setting
    ) {

    override val viewModel: SettingDialogViewModel =
        SettingDialogViewModel()

    private val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val value = viewModel.saveAction.get()
            if (value != null && value.isNotBlank()) {
                val dialog =
                    CommonDialogFragment(
                        text = String.format(resources.getString(R.string.ask_save), value),
                        _positiveCallback = {
                            val save = SettingCache(
                                ledLevel = viewModel.ledLevel.get(),
                                microCurrent = viewModel.microCurrentLevel.get()
                            )
                            if (value == "1") {
                                CredentialManager.instance.setting1 = save
                            } else {
                                CredentialManager.instance.setting2 = save
                            }
                        }
                    )
                dialog.show(requireActivity().supportFragmentManager, "")
            }
        }
    }

    private val okCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val value = viewModel.okAction.get()
            if (value != null && value.isNotBlank()) {
                CommonDialogFragment(
                    text = resources.getString(R.string.confirm_setting),
                    _positiveCallback = {
                        MainActivity.settingOkTriggerSubject.onNext(
                            SettingCache(
                                ledLevel = viewModel.ledLevel.get(),
                                microCurrent = viewModel.microCurrentLevel.get()
                            )
                        )
                        dismiss()
                    }
                ).show(requireActivity().supportFragmentManager, "")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        viewModel.saveAction.addOnPropertyChangedCallback(callback)
        viewModel.okAction.addOnPropertyChangedCallback(okCallback)
        viewModel.isKorean.set(ConfigurationCompat.getLocales(resources.configuration)[0].language == "ko")
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.saveAction.removeOnPropertyChangedCallback(callback)
        viewModel.okAction.removeOnPropertyChangedCallback(okCallback)
    }

}