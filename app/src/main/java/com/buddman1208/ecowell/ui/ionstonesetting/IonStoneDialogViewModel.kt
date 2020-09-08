package com.buddman1208.ecowell.ui.ionstonesetting

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseDialogViewModel
import com.buddman1208.ecowell.utils.CredentialManager
import com.buddman1208.ecowell.utils.clearAndSet

class IonStoneDialogViewModel : BaseDialogViewModel() {

    val isKorean: ObservableBoolean = ObservableBoolean(true)

    // level values
    val ledLevel: ObservableInt = ObservableInt(CredentialManager.instance.setting1.ledLevel)
    val microCurrentLevel: ObservableInt =
        ObservableInt(CredentialManager.instance.setting1.microCurrent)
    val galvanicLevel: ObservableInt = ObservableInt(1)

    // setting type
    val settingType: ObservableInt = ObservableInt(1)
    fun selectType(value: Int) {
        settingType.set(value)
        ledLevel.set(
            (if (value == 1) CredentialManager.instance.setting1 else CredentialManager.instance.setting2).ledLevel
        )
        microCurrentLevel.set(
            (if (value == 1) CredentialManager.instance.setting1 else CredentialManager.instance.setting2).microCurrent
        )
    }

    // save action
    val saveAction: ObservableField<String> = ObservableField("")
    fun onNextClicked() {
        saveAction.clearAndSet(settingType.get().toString())
    }

    fun onDismissClicked() = dismissEvent.clearAndSet("dismiss")

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