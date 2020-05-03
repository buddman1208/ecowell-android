package com.buddman1208.ecowell.ui.productselect

import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseViewModel
import com.buddman1208.ecowell.ui.main.MainActivity
import com.buddman1208.ecowell.utils.clearAndSet
import org.jetbrains.anko.toast

class ProductSelectViewModel : BaseViewModel() {

    val isLuwell: ObservableBoolean = ObservableBoolean(true)

    fun onTypeSelected(view: View) {
        // TODO ionstone not allowed for now
//        isLuwell.set(view.id == R.id.btnLuWellSelect)
        if(view.id == R.id.btnIonSelect) {
            view.context.toast("아직 사용할 수 없습니다.")
        }
    }

    fun onBluetoothButtonClicked(view : View) {
        activityToStart.clearAndSet(
            Pair(MainActivity::class, bundleOf())
        )
    }
}