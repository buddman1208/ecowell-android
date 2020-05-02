package com.buddman1208.ecowell.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.buddman1208.ecowell.view.RoundProgressView
import com.shawnlin.numberpicker.NumberPicker
import com.tsengvn.typekit.Typekit

class DataBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter("imageRes")
        fun loadImageRes(imageView: ImageView, resId: Int) {
            imageView.setImageResource(resId)
        }

        @JvmStatic
        @BindingAdapter("iconOnRight")
        fun loadDrawableRight(tv: TextView, resId: Int) {
            val iconOnRight = ContextCompat.getDrawable(tv.context, resId)
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null, iconOnRight, null)
        }

        @JvmStatic
        @BindingAdapter("progress")
        fun loadProgress(pv: RoundProgressView, progress: Int) {
            pv.progress = progress
        }

        @JvmStatic
        @BindingAdapter("pickerFont")
        fun setFontToNumberPicker(np: NumberPicker, font : String) {
            np.typeface = Typekit.createFromAsset(np.context, font)
            np.setSelectedTypeface(Typekit.createFromAsset(np.context, font))
        }

    }
}

