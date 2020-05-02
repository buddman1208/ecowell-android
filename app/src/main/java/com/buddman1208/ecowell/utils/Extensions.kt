package com.buddman1208.ecowell.utils

import android.content.res.Resources
import androidx.databinding.ObservableField

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun <T> ObservableField<T>.clearAndSet(value : T) {
    set(null)
    set(value)
}

