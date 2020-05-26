package com.buddman1208.ecowell.utils

import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable

object BLEController {

    private lateinit var bleClient: RxBleClient

    fun initialize(context: Context) {
        bleClient = RxBleClient.create(context)
    }

    fun getDeviceListStream(): Observable<ScanResult> {
        return bleClient.scanBleDevices(
            ScanSettings.Builder().build(),
            ScanFilter.empty()
        )
    }

    fun connectStream(macAddress: String): RxBleDevice {
        return bleClient.getBleDevice(macAddress)
    }


}