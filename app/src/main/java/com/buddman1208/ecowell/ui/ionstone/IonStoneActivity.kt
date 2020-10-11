package com.buddman1208.ecowell.ui.ionstone

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityIonstoneBinding
import com.buddman1208.ecowell.ui.base.BaseActivity
import com.buddman1208.ecowell.ui.ionstonesetting.IonStoneSettingCompleteListener
import com.buddman1208.ecowell.ui.ionstonesetting.IonStoneSettingFragment
import com.buddman1208.ecowell.ui.ionstonesetting.IonStoneSettingResponse
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import com.buddman1208.ecowell.ui.setting.SettingDialogFragment
import com.buddman1208.ecowell.utils.BLEController
import com.buddman1208.ecowell.utils.IonStoneRequestConverter
import com.buddman1208.ecowell.utils.SettingCache
import com.buddman1208.ecowell.utils.toStringArray
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class IonStoneActivity : BaseActivity<ActivityIonstoneBinding, IonStoneViewModel>(
    R.layout.activity_ionstone
) {
    public override val viewModel: IonStoneViewModel =
        IonStoneViewModel()

    val macaddress: String by lazy {
        intent.getStringExtra("macAddress") ?: ""
    }

    private lateinit var bleDevice: RxBleDevice
    private val disconnectTriggerSubject = PublishSubject.create<String>()

    private lateinit var bleObservable: Observable<RxBleConnection>

    val writeUUID: UUID by lazy {
        intent.getSerializableExtra("write") as UUID
    }

    val notifyUUID: UUID by lazy {
        intent.getSerializableExtra("notify") as UUID
    }

    private var timeLeft: Int = 0
    private var handler: Handler? = Handler()

    var time = System.currentTimeMillis()

    private fun timerRunnable(): Runnable = Runnable {
        timeLeft -= 1
        Log.e("asdf", "${System.currentTimeMillis() - time}")
        time = System.currentTimeMillis()

        updateProgress()
        handler?.postDelayed(timerRunnable(), 1000L)
    }

    private val settingInstance: SettingDialogFragment by lazy {
        val instance = SettingDialogFragment::class.java.newInstance()
        instance
    }

    private val eventCallback =
        object : androidx.databinding.Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(
                sender: androidx.databinding.Observable?,
                propertyId: Int
            ) {
                viewModel.event.get()?.run {
                    when (this) {
                        "openSetting" -> {
                            settingInstance.show(supportFragmentManager, "")
                        }
                        else -> {
                        }
                    }
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
        viewModel.isKorean.set(ConfigurationCompat.getLocales(resources.configuration)[0].language == "ko")

        validateConnection()
        disconnectTriggerSubject
            .subscribe { compositeDisposable.clear() }
            .let { compositeDisposable.add(it) }

        binding.apply {
            ivRun.setOnClickListener {
//                if (viewModel.isModeSelected.get()) {
//                    if (viewModel.isRunning.get()) {
//                        stopTimer()
//                        viewModel.isRunning.set(false)
//                    } else {
//                        startTimer()
//                        viewModel.isRunning.set(true)
//                    }
//                }
                write(
                    IonStoneRequestConverter.getPlayTimeSettingRequest()
                )
            }
            ivMode.setOnClickListener {
//                openDialog()
                write(
                    IonStoneRequestConverter.getPlayRequest()
                )

            }
            ivWater.setOnClickListener {
//                openDialog()
                write(
                    IonStoneRequestConverter.getPauseRequest()
                )

            }
            ivAdditives.setOnClickListener {
//                openDialog()
                write(
                    IonStoneRequestConverter.getStopRequest()
                )

            }
        }
    }

    private fun validateConnection() {
        bleDevice = BLEController.connectStream(macaddress)
        bleObservable = prepareConnectionObservable()


        bleObservable
            .flatMapSingle { it.discoverServices() }
            .flatMapSingle { it.getCharacteristic(notifyUUID) }
            .observeOn(AndroidSchedulers.mainThread())
            .takeUntil(disconnectTriggerSubject)
            .doOnSubscribe { toast(resources.getString(R.string.connecting)) }
            .subscribe({
                toast(resources.getString(R.string.connected))
            }, {})
            .let { compositeDisposable.add(it) }

        bleObservable
            .flatMap { it.setupNotification(notifyUUID) }
            .doOnNext {
                runOnUiThread {
                    toast(resources.getString(R.string.connected_alert))

                    viewModel.isBluetoothEnabled.set(true)
                    write(
                        IonStoneRequestConverter.getAllScanRequest()
                    )
                }
            }
            .flatMap { it }
            .observeOn(AndroidSchedulers.mainThread())
            .takeUntil(disconnectTriggerSubject)
            .subscribe(::onNotificationReceived, ::onConnectionError)
            .let { compositeDisposable.add(it) }

    }

    private fun write(writeTarget: ByteArray) {
        // "X7L:2000078N"
        bleObservable
            .firstOrError()
            .flatMap { it.writeCharacteristic(writeUUID, writeTarget) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.e("asdf", "write success ${it.toStringArray().joinToString(", ")}")
            }, ::onConnectionError)
            .let { compositeDisposable.add(it) }
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
        bleDevice
            .establishConnection(true)
            .takeUntil(disconnectTriggerSubject)
            .compose(ReplayingShare.instance())

    private fun onNotificationReceived(bytes: ByteArray) {
        val bytes = bytes.map { it.toUInt().toString(16) }
        Log.e("asdf", bytes.joinToString(",  "))
    }

    private fun openDialog() {
        IonStoneSettingFragment(object : IonStoneSettingCompleteListener {
            override fun onComplete(response: IonStoneSettingResponse) {
                viewModel.isModeSelected.set(true)
                updateTimer()
                viewModel.modeString.set(
                    when (response.mode) {
                        0 -> "위생용품"
                        1 -> "홈케어"
                        2 -> "전자제품"
                        else -> ""
                    }
                )
                viewModel.waterString.set(
                    when (response.water) {
                        0 -> "2L"
                        1 -> "3L"
                        2 -> "4L"
                        else -> ""
                    }
                )
                viewModel.additiveString.set(
                    "Salt\n2g"
                )
            }
        }).show(supportFragmentManager, "")
    }

    private fun showDialogAndFinish(state: MainBluetoothState) {
    }

    private fun updateTimer(leftSeconds: Int = MIN_20) {
        timeLeft = leftSeconds
        updateProgress()
    }

    private fun startTimer() {
        handler?.removeCallbacksAndMessages(null)
        handler?.postDelayed(timerRunnable(), 1000)
        updateProgress()
    }

    private fun stopTimer() {
        handler?.removeCallbacksAndMessages(null)
        updateProgress()
    }

    private fun updateProgress() {
        viewModel.currentTime.set(
            "${String.format(
                "%02d",
                timeLeft / 60
            )} : ${String.format("%02d", timeLeft % 60)}"
        )
        val progress = timeLeft * 100 / MIN_20
        viewModel.progress.set(progress)
    }

    private fun getBatteryStatus(batteryLevel: Int): BatteryLevel {
        return when (batteryLevel) {
            0 -> BatteryLevel.NO
            1 -> BatteryLevel.LOW
            2 -> BatteryLevel.MIDDLE
            else -> BatteryLevel.FULL
        }
    }

    private fun onConnectionError(throwable: Throwable) {
        if (throwable is BleDisconnectedException) {
            showDialogAndFinish(MainBluetoothState.BLUETOOTH_DISCONNECTED)
        } else showDialogAndFinish(MainBluetoothState.BLUETOOTH_ERROR)
    }

    override fun onPause() {
        super.onPause()
        viewModel.event.removeOnPropertyChangedCallback(eventCallback)
    }

    override fun onResume() {
        super.onResume()
        viewModel.event.addOnPropertyChangedCallback(eventCallback)
    }

    companion object {
        const val MIN_20: Int = 20 * 60
        val settingOkTriggerSubject = PublishSubject.create<SettingCache>()

    }

    override fun onBackPressed() {
        startActivity<ProductSelectActivity>()
        finish()
        handler?.removeCallbacksAndMessages(null)
    }

}

enum class MainBluetoothState {
    COMPLETE,
    BLUETOOTH_ERROR,
    BLUETOOTH_DISCONNECTED,
    LOW_BATTERY
}