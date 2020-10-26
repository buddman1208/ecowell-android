package com.buddman1208.ecowell.ui.productselect

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.core.os.ConfigurationCompat
import androidx.databinding.Observable
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityProductSelectBinding
import com.buddman1208.ecowell.ui.base.BaseActivity
import com.buddman1208.ecowell.ui.bluetooth.BleDeviceActivity
import com.buddman1208.ecowell.ui.ionstone.IonStoneActivity
import com.buddman1208.ecowell.ui.luwell.LuwellActivity
import com.buddman1208.ecowell.utils.BLEController
import com.buddman1208.ecowell.utils.CredentialManager
import com.buddman1208.ecowell.utils.DeviceCache
import com.buddman1208.ecowell.utils.LocaleWrapper
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_product_select.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class ProductSelectActivity : BaseActivity<ActivityProductSelectBinding, ProductSelectViewModel>(
    R.layout.activity_product_select
) {
    override var viewModel: ProductSelectViewModel =
        ProductSelectViewModel()

    val onSearchCompleteSubject: PublishSubject<String> = PublishSubject.create()

    private var luwellCache: DeviceCache? = null
    private var ionStoneCache: DeviceCache? = null

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
                        BLEController.connectStream(luwellCache?.macAddress ?: "")
                            .establishConnection(false)
                            .flatMapSingle { connection -> connection.discoverServices() }
                            .take(1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e("asdf", it.toString())
                                // nordi : property 12 write property 16 notify
                                val write =
                                    it.bluetoothGattServices.map { it.characteristics }.flatten()
                                        .find { it.properties == 12 || it.properties == BluetoothGattCharacteristic.PROPERTY_WRITE }?.uuid
                                val notify =
                                    it.bluetoothGattServices.map { it.characteristics }.flatten()
                                        .find { it.properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY }?.uuid

                                startActivity(
                                    intentFor<LuwellActivity>(
                                        "macAddress" to luwellCache?.macAddress,
                                        "write" to write,
                                        "notify" to notify
                                    )
                                )
                                finish()

                            }, {
                                toast("오류가 발생했습니다.\n${it.message}")
                                if (it is BleScanException) {
                                    toast(resources.getString(R.string.request_bluetooth_on))
                                    finish()
                                } else {
                                    it.printStackTrace()
                                }
                            })
                    }
                    "startIonStoneActivity" -> {
                        BLEController.connectStream(ionStoneCache?.macAddress ?: "")
                            .establishConnection(false)
                            .flatMapSingle { connection -> connection.discoverServices() }
                            .take(1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e("asdf", it.toString())
                                // nordi : property 12 write property 16 notify
                                val write =
                                    it.bluetoothGattServices.map { it.characteristics }.flatten()
                                        .find { it.properties == 12 || it.properties == BluetoothGattCharacteristic.PROPERTY_WRITE }?.uuid
                                val notify =
                                    it.bluetoothGattServices.map { it.characteristics }.flatten()
                                        .find { it.properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY }?.uuid

                                startActivity(
                                    intentFor<IonStoneActivity>(
                                        "macAddress" to ionStoneCache?.macAddress,
                                        "write" to write,
                                        "notify" to notify
                                    )
                                )
                                finish()

                            }, {
                                toast("오류가 발생했습니다.\n${it.message}")
                                if (it is BleScanException) {
                                    toast(resources.getString(R.string.request_bluetooth_on))
                                    finish()
                                } else {
                                    it.printStackTrace()
                                }
                            })
                    }
                    "onKoreanSelected" -> {
                        changeLanguage("ko")
                    }
                    "onEnglishSelected" -> {
                        changeLanguage("en")
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun changeLanguage(language: String) {
        LocaleWrapper.setLocale(language)
        viewModel.isKorean.set(language == "ko")
        initStrings()
    }

    private fun subscribeDevices() {
        BLEController
            .getDeviceListStream()
            .filter {
                it.bleDevice.name?.toUpperCase(Locale.ROOT)?.contains("CELL_POD") == true ||
                        it.bleDevice.name?.toUpperCase(Locale.ROOT)?.contains("MONSTERBALL") == true
            }
            .takeUntil(onSearchCompleteSubject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::updateTarget) {
                if (it is BleScanException) {
                    toast("블루투스를 허용해주세요.")
                }
                longToast(it.message.toString())
            }
            .let { compositeDisposable.add(it) }
    }

    private fun updateTarget(result: ScanResult) {
        val macaddress = result.bleDevice.macAddress
        val devicename = result.bleDevice.name ?: ""

        val isTarget =
            macaddress == luwellCache?.macAddress ?: "" ||
                    macaddress == ionStoneCache?.macAddress ?: ""

        if (isTarget) {
            val isLuwell = devicename.contains("CELL_POD")

            if (isLuwell) viewModel.luWellAvailable.set(true)
            else viewModel.ionStoneAvailable.set(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
        viewModel.event.addOnPropertyChangedCallback(eventCallback)

        luwellCache = CredentialManager.instance.luwellCache
        ionStoneCache = CredentialManager.instance.ionstoneCache
        subscribeDevices()

        val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
        viewModel.isKorean.set(locale.language == "ko")

        initStrings()
    }

    private fun initStrings() {
        var conf: Configuration = resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale(CredentialManager.instance.language))
        val localizedContext = createConfigurationContext(conf)
        val localRes = localizedContext.resources

        tvTitle.text = localRes.getString(R.string.ecowell_product_select)
        btnBluetoothConnection.text = localRes.getString(R.string.bluetooth_connection)
        tvLanguage.text = localRes.getString(R.string.language)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BLUETOOTH_ACTIVITY_RESULT_CODE && resultCode == RESULT_OK) {
            data?.extras?.run {
                val productName = getString("productName") ?: ""
                val macAddress = getString("macAddress") ?: ""
//                val writeUUID = getSerializable("write") as UUID?
//                val notifyUUID = getSerializable("notify") as UUID?

                if (listOf(
                        macAddress
//                        writeUUID.toString(),
//                        notifyUUID.toString()
                    ).none { it.isBlank() }
                ) {
                    val device = DeviceCache(
                        macAddress
                    )
                    val isLuwell = productName.contains("CELL_POD")
                    if (isLuwell) {
                        CredentialManager.instance.luwellCache = device
                        this@ProductSelectActivity.luwellCache = device
                    } else {
                        CredentialManager.instance.ionstoneCache = device
                        this@ProductSelectActivity.ionStoneCache = device
                    }
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
