package com.buddman1208.ecowell.utils

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.databinding.ObservableField
import com.buddman1208.ecowell.AppController
import java.util.*


val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun <T> ObservableField<T>.clearAndSet(value : T) {
    set(null)
    set(value)
}

fun getCurrentLocale(): Locale {
    return ConfigurationCompat.getLocales(AppController.context.resources.configuration)[0]
}

fun changeLocale() {
    LocaleWrapper.setLocale(if (getCurrentLocale().language == "ko") "en" else "ko")
}
