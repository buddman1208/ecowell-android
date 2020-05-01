package com.buddman1208.ecowell.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

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
    }
}