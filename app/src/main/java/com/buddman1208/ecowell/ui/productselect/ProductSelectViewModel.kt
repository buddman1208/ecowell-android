package com.buddman1208.ecowell.ui.productselect

import android.view.View
import androidx.databinding.ObservableBoolean
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseViewModel
import com.buddman1208.ecowell.utils.clearAndSet
import org.jetbrains.anko.toast

class ProductSelectViewModel : BaseViewModel() {

    val luWellAvailable: ObservableBoolean = ObservableBoolean(false)

    fun onTypeSelected(view: View) {
        // TODO ionstone not allowed for now
        when (view.id) {
            R.id.btnIonSelect -> view.context.toast("연결된 기기가 없습니다.")
            R.id.btnLuWellSelect -> if(luWellAvailable.get()) {
                event.clearAndSet("startLuWellActivity")
            } else view.context.toast("연결된 기기가 없습니다.")
        }
    }

    fun onBluetoothButtonClicked(view: View) {
        event.clearAndSet("startBluetoothActivity")
    }
}