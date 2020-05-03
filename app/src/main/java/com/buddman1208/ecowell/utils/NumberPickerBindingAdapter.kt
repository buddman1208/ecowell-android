package com.buddman1208.ecowell.utils

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.shawnlin.numberpicker.NumberPicker

class NumberPickerBindingAdapter {
    companion object {
        // number-picker
        @JvmStatic
        @BindingAdapter("npValue")
        fun setNumberPickerValue(np: NumberPicker, value: Int) {
            np.value = value
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "npValue", event = "npValueAttrChanged")
        fun getNumberPickerValue(np: NumberPicker): Int = np.value

        @JvmStatic
        @BindingAdapter("npValueAttrChanged")
        fun setNumberPickerValueChangedListener(
            np: NumberPicker,
            listener: InverseBindingListener
        ) {
            np.setOnValueChangedListener { _, _, _ -> listener.onChange() }
        }
    }
}

