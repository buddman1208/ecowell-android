package com.buddman1208.ecowell.ui.setting

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseDialogViewModel
import com.buddman1208.ecowell.utils.clearAndSet

class SettingDialogViewModel : BaseDialogViewModel() {

    // level values
    val ledLevel: ObservableInt = ObservableInt(1)
    val microCurrentLevel: ObservableInt = ObservableInt(1)
    val galvanicLevel: ObservableInt = ObservableInt(1)

    // setting type
    val settingType: ObservableInt = ObservableInt(1)
    fun selectType(value: Int) = settingType.set(value)

    // save action
    val saveAction: ObservableField<String> = ObservableField("")
    fun onSaveClicked() {
        saveAction.clearAndSet(settingType.get().toString())
    }

    companion object {
        // type select
        @JvmStatic
        @BindingAdapter("selectedValue")
        fun setTextStyleByValue(tv: TextView, value: Int) {
            when (tv.id) {
                R.id.tvType1 -> setButtonStyle(
                    tv,
                    (value == 1)
                )
                R.id.tvType2 -> setButtonStyle(
                    tv,
                    (value == 2)
                )
            }
        }

        private fun setButtonStyle(tv: TextView, isSelected: Boolean) {
            TextViewCompat.setTextAppearance(
                tv,
                if (isSelected) R.style.SelectedTextView else R.style.NotSelectedTextView
            )
            tv.setBackgroundResource(
                if (isSelected) R.drawable.bg_oval_accent
                else R.drawable.bg_oval_gray
            )
        }
    }
}