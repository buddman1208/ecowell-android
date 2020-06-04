package com.buddman1208.ecowell.ui.productselect

import android.content.Intent
import android.os.Bundle
import androidx.core.os.ConfigurationCompat
import androidx.databinding.Observable
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityProductSelectBinding
import com.buddman1208.ecowell.ui.base.BaseActivity
import com.buddman1208.ecowell.ui.bluetooth.BleDeviceActivity
import com.buddman1208.ecowell.ui.main.MainActivity
import com.buddman1208.ecowell.utils.BLEController
import com.buddman1208.ecowell.utils.CredentialManager
import com.buddman1208.ecowell.utils.DeviceCache
import com.buddman1208.ecowell.utils.LocaleWrapper
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.intentFor
import java.util.*

class ProductSelectActivity : BaseActivity<ActivityProductSelectBinding, ProductSelectViewModel>(
    R.layout.activity_product_select
) {
    override var viewModel: ProductSelectViewModel =
        ProductSelectViewModel()

    val onSearchCompleteSubject: PublishSubject<String> = PublishSubject.create()

    private var deviceCache: DeviceCache? = null

    val BLUETOOTH_ACTIVITY_RESULT_CODE = 6799

    private val eventCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            viewModel.event.get()?.run {
                when (this) {
                    "startBluetoothActivity" -> {
                        startActivityForResult(
                            Intent(this@ProductSelectActivity, BleDeviceActivity::class.java),
                            BLUETOOTH_ACTIVITY_RESULT_CODE
                        )
                    }
                    "startLuWellActivity" -> {
                        startActivity(
                            intentFor<MainActivity>(
                                "macAddress" to deviceCache?.macAddress,
                                "write" to deviceCache?.writeUUID,
                                "notify" to deviceCache?.notifyUUID
                            )
                        )
                        finish()
                    }
                    "onKoreanSelected" -> {
                        changeLanguage("ko")
                    }
                    "onEnglishSelected" -> {
                        changeLanguage("en")
                    }
                }
            }
        }
    }

    private fun changeLanguage(language: String) {
        LocaleWrapper.setLocale(language)
        recreate()
    }

    private fun subscribeDevices() {
        BLEController
            .getDeviceListStream()
            .filter {
                it.bleDevice.name?.toUpperCase(Locale.ROOT)?.contains("CELL_POD") == true
            }
            .takeUntil(onSearchCompleteSubject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::updateTarget) {
//                if (it is BleScanException) {
//                    toast("블루투스를 허용해주세요.")
//                }
//                longToast(it.message.toString())
            }
            .let { compositeDisposable.add(it) }
    }

    private fun updateTarget(result: ScanResult) {
        val isTarget = (result.bleDevice.macAddress == deviceCache?.macAddress ?: "")
        if(isTarget) {
            viewModel.luWellAvailable.set(true)
            onSearchCompleteSubject.onNext("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
        viewModel.event.addOnPropertyChangedCallback(eventCallback)

        deviceCache = CredentialManager.instance.deviceCache
        subscribeDevices()

        val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
        viewModel.isKorean.set(locale.language == "ko")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BLUETOOTH_ACTIVITY_RESULT_CODE && resultCode == RESULT_OK) {
            data?.extras?.run {
                val macAddress = getString("macAddress") ?: ""
                val writeUUID = getSerializable("write") as UUID?
                val notifyUUID = getSerializable("notify") as UUID?

                if (listOf(macAddress, writeUUID.toString(), notifyUUID.toString()).none { it.isBlank() }) {
                    val device = DeviceCache(
                        macAddress, writeUUID!!, notifyUUID!!
                    )
                    CredentialManager.instance.deviceCache = device
                    this@ProductSelectActivity.deviceCache = device
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        viewModel.event.removeOnPropertyChangedCallback(eventCallback)
        super.onDestroy()
    }

}
