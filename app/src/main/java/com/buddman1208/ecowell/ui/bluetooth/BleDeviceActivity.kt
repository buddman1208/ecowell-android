package com.buddman1208.ecowell.ui.bluetooth

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
        supportActionBar?.title = resources.getString(R.string.search_device)

        initBleList()
        initList()
    }

    private fun initBleList() {
        if (isBleEnabled()) {
            BLEController
                .getDeviceListStream()
                .filter {
                    it.bleDevice.name?.toUpperCase(Locale.ROOT)?.contains("CELL_POD") == true ||
                            it.bleDevice.name?.toUpperCase(Locale.ROOT)
                                ?.contains("MONSTERBALL") == true
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::updateList) {
                    if (it is BleScanException) {
                        toast(resources.getString(R.string.request_bluetooth_on))
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
                    val deviceName = item.bleDevice.name ?: ""

                    returnSelected(deviceName, mac)
                    //todo parameter change
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

    private fun returnSelected(productName: String, macAddress: String) {
        val intent = Intent().apply {
            putExtras(
                bundleOf(
                    "productName" to productName,
                    "macAddress" to macAddress
                )
            )
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
