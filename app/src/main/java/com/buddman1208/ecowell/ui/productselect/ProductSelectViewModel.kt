package com.buddman1208.ecowell.ui.productselect

import android.view.View
import androidx.databinding.ObservableBoolean
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseViewModel
import com.buddman1208.ecowell.utils.clearAndSet
import org.jetbrains.anko.toast

class ProductSelectViewModel : BaseViewModel() {

    val luWellAvailable: ObservableBoolean = ObservableBoolean(false)
    val ionStoneAvailable: ObservableBoolean = ObservableBoolean(true)
    val isKorean: ObservableBoolean = ObservableBoolean(true)

    fun onKoreanSelected() {
        event.clearAndSet("onKoreanSelected")
    }

    fun onEnglishSelected() {
        event.clearAndSet("onEnglishSelected")
    }

    fun onTypeSelected(view: View) {
        // TODO ionstone not allowed for now
        when (view.id) {
            R.id.btnIonSelect -> event.clearAndSet("startLuWellActivity")
//            R.id.btnIonSelect -> event.clearAndSet("startIonStoneActivity")
            R.id.btnLuWellSelect -> if (luWellAvailable.get()) {
                event.clearAndSet("startLuWellActivity")
            } else view.context.toast(view.context.resources.getString(R.string.no_device_connected))
        }
    }

    fun onBluetoothButtonClicked(view: View) {
        event.clearAndSet("startBluetoothActivity")
    }
}