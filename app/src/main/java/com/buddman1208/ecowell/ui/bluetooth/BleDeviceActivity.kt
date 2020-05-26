package com.buddman1208.ecowell.ui.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityBluetoothDevicesBinding
import com.buddman1208.ecowell.ui.base.BaseActivity
import com.buddman1208.ecowell.utils.BLEController
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_bluetooth_devices.*
import org.jetbrains.anko.toast
import java.util.*

class BleDeviceActivity : BaseActivity<ActivityBluetoothDevicesBinding, BleDeviceViewModel>(
    R.layout.activity_bluetooth_devices
) {
    override val viewModel: BleDeviceViewModel = BleDeviceViewModel()

    val BLUETOOTH_ACTIVITY_RESULT_CODE = 6799

    val dataSource = dataSourceTypedOf<ScanResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기기 검색"

        initBleList()
        initList()
    }

    private fun initBleList() {
        if (isBleEnabled()) {
            BLEController
                .getDeviceListStream()
                .filter {
                    it.bleDevice.name?.toUpperCase(Locale.ROOT)?.contains("CELL_POD") == true
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::updateList) {
                    if (it is BleScanException) {
                        toast("블루투스를 허용해주세요.")
                        finish()
                    }
                }
                .let { compositeDisposable.add(it) }
        }
    }

    private fun initList() {

        rv.layoutManager = LinearLayoutManager(rv.context)
        rv.addItemDecoration(
            DividerItemDecoration(
                rv.context,
                DividerItemDecoration.VERTICAL
            )
        )
        rv.setup {
            withDataSource(dataSource)
            withItem<ScanResult, ScanResultItemViewHolder>(R.layout.content_bluetooth_list) {
                onBind(::ScanResultItemViewHolder) { index, item ->
                    title.text =
                        item.bleDevice.name + "\n" + item.bleDevice.macAddress
                }
                onClick { index ->
                    val mac = item.bleDevice.macAddress
                    BLEController.connectStream(mac)
                        .establishConnection(false)
                        .flatMapSingle { connection -> connection.discoverServices() }
                        .take(1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.e("asdf", it.toString())
                            val write =
                                it.bluetoothGattServices.map { it.characteristics }.flatten()
                                    .find { it.properties == BluetoothGattCharacteristic.PROPERTY_WRITE }?.uuid
                            val notify =
                                it.bluetoothGattServices.map { it.characteristics }.flatten()
                                    .find { it.properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY }?.uuid
                            returnSelected(mac, write, notify)
                        }, {
                            if (it is BleScanException) {
                                toast("블루투스를 허용해주세요.")
                                finish()
                            } else {
                                it.printStackTrace()
                            }
                        })
                        .let { compositeDisposable.add(it) }

                }
            }
        }
    }

    @Synchronized
    private fun updateList(result: ScanResult) {
        Log.e("asdf", result.toString())
        try {
            dataSource.apply {
                if (toList().none { it.bleDevice.macAddress == result.bleDevice.macAddress }) {
                    add(result)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isBleEnabled(): Boolean = true

    private fun returnSelected(macAddress: String, write: UUID?, notify: UUID?) {
        if (write != null && notify != null) {
            val intent = Intent().apply {
                putExtras(
                    bundleOf(
                        "macAddress" to macAddress,
                        "write" to write,
                        "notify" to notify
                    )
                )
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}
