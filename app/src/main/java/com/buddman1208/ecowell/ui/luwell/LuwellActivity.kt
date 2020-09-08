package com.buddman1208.ecowell.ui.luwell

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityLuwellBinding
import com.buddman1208.ecowell.ui.base.BaseActivity
import com.buddman1208.ecowell.ui.commondialog.CommonDialogFragment
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import com.buddman1208.ecowell.ui.setting.SettingDialogFragment
import com.buddman1208.ecowell.utils.BLEController
import com.buddman1208.ecowell.utils.EcoWellStatus
import com.buddman1208.ecowell.utils.RequestConverter
import com.buddman1208.ecowell.utils.SettingCache
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

class LuwellActivity : BaseActivity<ActivityLuwellBinding, LuwellViewModel>(
    R.layout.activity_luwell
) {
    public override val viewModel: LuwellViewModel =
        LuwellViewModel()

    val macaddress: String by lazy {
        intent.getStringExtra("macAddress")
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

    private var isCompleteOrErrorOccurred: Boolean = false

    private var timeLeft: Int = 0
    private var countTimer: Timer? = null
    private var handler: Handler? = Handler()

    var time = System.currentTimeMillis()

    private fun timerRunnable(): Runnable = Runnable {
        timeLeft -= 1
        Log.e("asdf", "${System.currentTimeMillis() - time}")
        time = System.currentTimeMillis()

        updateProgress()
        if (timeLeft == 10 * 60 + 30 || timeLeft == 30) {
            // 10:30 초 남았을때, 30초 남았을 때
            write(
                RequestConverter.sendTimeRequest(timeLeft / 60, timeLeft % 60)
            )
        }

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
                        "onLedClick" -> {
                            val level = viewModel.ledLevel.get()
                            if (level > 0) {
                                val targetLedLevel = if (level < 3) level + 1 else 1
                                write(
                                    RequestConverter.setLedLevel(
                                        targetLedLevel,
                                        timeLeft / 60,
                                        timeLeft % 60
                                    )
                                )
//                                Handler().postDelayed({
//                                    write(
//                                        RequestConverter.setParameterSaveEnabled(true)
//                                    )
//                                }, 100)
                            }
                        }
                        "onMicroCurrentClick" -> {
                            val level = viewModel.microCurrentLevel.get()
                            if (level > 0) {
                                val targetMicroCurrentLevel = if (level < 5) level + 1 else 1
                                write(
                                    RequestConverter.setExportLevel(
                                        targetMicroCurrentLevel,
                                        timeLeft / 60,
                                        timeLeft % 60
                                    )
                                )
//                                Handler().postDelayed({
//                                    write(
//                                        RequestConverter.setParameterSaveEnabled(true)
//                                    )
//                                }, 100)
                            }
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
                write(
                    RequestConverter.playStopRequest(
                        !viewModel.isRunning.get(),
                        timeLeft / 60,
                        timeLeft % 60
                    )
                )
            }
        }
        settingOkTriggerSubject.subscribe {
            write(
                RequestConverter.setLedLevel(it.ledLevel, timeLeft / 60, timeLeft % 60)
            )
            Handler().postDelayed({
                write(
                    RequestConverter.setExportLevel(it.microCurrent, timeLeft / 60, timeLeft % 60)
                )
                Handler().postDelayed({
                    write(
                        RequestConverter.setParameterSaveEnabled(true)
                    )
                }, 100)
            }, 100)

        }.let { compositeDisposable.add(it) }
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
                        RequestConverter.getAllScanRequest()
                    )
                }
            }
            .flatMap { it }
            .observeOn(AndroidSchedulers.mainThread())
            .takeUntil(disconnectTriggerSubject)
            .subscribe(::onNotificationReceived, ::onConnectionError)
            .let { compositeDisposable.add(it) }

    }

    private fun write(writeString: String) {
        // "X7L:2000078N"
        bleObservable
            .firstOrError()
            .flatMap { it.writeCharacteristic(writeUUID, writeString.toByteArray()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.e("asdf", "write success ${RequestConverter.parseStatus(it)}.")
            }, ::onConnectionError)
            .let { compositeDisposable.add(it) }
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
        bleDevice
            .establishConnection(true)
            .takeUntil(disconnectTriggerSubject)
            .compose(ReplayingShare.instance())

    private fun onNotificationReceived(bytes: ByteArray) {
        if (bytes != null && bytes.isNotEmpty()) {
            Log.e("Asdf", bytes.joinToString(" "))
            Log.e("Asdf", bytes.map { it.toChar() }.joinToString(" "))
            Log.e("noti received", RequestConverter.parseStatus(bytes).toString())

            val deviceStatus = RequestConverter.parseStatus(bytes)

            if (deviceStatus?.runMode ?: -1 == 0) {
                // disconnect trigger
                showDialogAndFinish(MainBluetoothState.COMPLETE)

            } else if (getBatteryStatus(deviceStatus?.batteryLevel ?: -1) == BatteryLevel.NO) {
                showDialogAndFinish(MainBluetoothState.LOW_BATTERY)
            } else {
                if (deviceStatus?.runMode ?: -1 == 4) {
                    viewModel.canTouchTutorial.set(false)
                    viewModel.isShowTutorial.set(true)
                } else {
                    if (!viewModel.canTouchTutorial.get()) {
                        viewModel.isShowTutorial.set(false)
                        viewModel.canTouchTutorial.set(true)
                    }
                }
            }
            updateViewModel(deviceStatus)
        }
    }


    private fun showDialogAndFinish(state: MainBluetoothState) {
        if (isCompleteOrErrorOccurred.not()) {
            isCompleteOrErrorOccurred = true
            viewModel.isBluetoothEnabled.set(false)
            disconnectTriggerSubject.onNext("")
            compositeDisposable.clear()
            CommonDialogFragment(
                text = when (state) {
                    MainBluetoothState.COMPLETE -> resources.getString(R.string.operation_completed)
                    MainBluetoothState.BLUETOOTH_ERROR -> resources.getString(R.string.bluetooth_error)
                    MainBluetoothState.BLUETOOTH_DISCONNECTED -> resources.getString(R.string.bluetooth_disconnected)
                    MainBluetoothState.LOW_BATTERY -> resources.getString(R.string.low_battery)
                },
                _positiveCallback = {
                    startActivity<ProductSelectActivity>()
                    finish()
                    countTimer?.cancel()
                },
                _isOnlyConfirmable = true,
                _isCancelable = false
            ).show(supportFragmentManager, "")
        }
    }

    private fun updateViewModel(deviceStatus: EcoWellStatus?) {
        deviceStatus?.run {
            viewModel.batteryLevel.set(getBatteryStatus(batteryLevel))
            viewModel.ledLevel.set(
                if (runMode != 4) ledLevel
                else 0
            )
            viewModel.microCurrentLevel.set(
                if (runMode == 1) exportLevel else 0
            )
            viewModel.galvanicIontoLevel.set(
                if (runMode == 2) 1 else 0
            )
            viewModel.isRunning.set(isRunning)
            if (countTimer == null) updateTimer(
                if (minute != -1 && second != -1) minute * 60 + second else MIN_20
            )
            if (isRunning) startTimer() else stopTimer()
        }
    }

    private fun updateTimer(leftSeconds: Int = MIN_20) {
        timeLeft = leftSeconds
        updateProgress()
    }

    private fun startTimer() {
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
        countTimer?.cancel()
    }

}

enum class MainBluetoothState {
    COMPLETE,
    BLUETOOTH_ERROR,
    BLUETOOTH_DISCONNECTED,
    LOW_BATTERY
}