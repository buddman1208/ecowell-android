package com.buddman1208.ecowell.ui.bluetooth

import android.view.View
import android.widget.TextView
import androidx.databinding.ObservableArrayList
import com.afollestad.recyclical.ViewHolder
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseViewModel
import com.polidea.rxandroidble2.scan.ScanResult

class BleDeviceViewModel : BaseViewModel() {

    val deviceList: ObservableArrayList<ScanResult> = ObservableArrayList()

}

class ScanResultItemViewHolder(view : View): ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.title)

}