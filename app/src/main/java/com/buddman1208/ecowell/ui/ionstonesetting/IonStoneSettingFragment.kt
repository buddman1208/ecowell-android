package com.buddman1208.ecowell.ui.ionstonesetting

import android.os.Bundle
import android.view.View
import androidx.core.os.ConfigurationCompat
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.DialogIonstoneSettingBinding
import com.buddman1208.ecowell.ui.base.BaseDialogFragment

class IonStoneSettingFragment(
    val listener: IonStoneSettingCompleteListener
) : BaseDialogFragment<DialogIonstoneSettingBinding, IonStoneDialogViewModel>(
    R.layout.dialog_ionstone_setting
) {

    override val viewModel: IonStoneDialogViewModel =
        IonStoneDialogViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        viewModel.isKorean.set(ConfigurationCompat.getLocales(resources.configuration)[0].language == "ko")

        binding.tvNext.setOnClickListener {
            if (!viewModel.isSecondPage.get()) {
                viewModel.isSecondPage.set(true)

                val mode = binding.wheelMode.currentItemPosition
                val water = binding.wheelWater.currentItemPosition

                viewModel.mode.set(
                    resources.getStringArray(R.array.ionstone_mode)[mode]
                )
                viewModel.water.set(when(water) {
                    0 -> 2
                    1 -> 3
                    2 -> 4
                    else -> 0
                })
                viewModel.minute.set(when(mode) {
                    0 -> 3
                    1 -> 5
                    2 -> 7
                    else -> 0
                })
            } else {
                val mode = binding.wheelMode.currentItemPosition
                val water = binding.wheelWater.currentItemPosition

                listener.onComplete(
                    IonStoneSettingResponse(
                        mode = mode,
                        water = water
                    )
                )
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

interface IonStoneSettingCompleteListener {
    fun onComplete(response: IonStoneSettingResponse)
}

data class IonStoneSettingResponse(
    val mode: Int,
    val water: Int
)