package com.buddman1208.ecowell.ui.ionstonesetting

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseDialogViewModel
import com.buddman1208.ecowell.utils.clearAndSet

class IonStoneDialogViewModel : BaseDialogViewModel() {

    val isKorean: ObservableBoolean = ObservableBoolean(true)
    val isSecondPage: ObservableBoolean = ObservableBoolean(false)

    val mode: ObservableField<String> = ObservableField("")
    val minute: ObservableField<Int> = ObservableField(0)
    val water: ObservableField<Int> = ObservableField(0)

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