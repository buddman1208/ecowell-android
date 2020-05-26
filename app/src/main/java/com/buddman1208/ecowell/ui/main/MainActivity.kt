package com.buddman1208.ecowell.ui.main

import android.os.Bundle
import android.util.Log
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityMainBinding
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

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main
) {
    public override val viewModel: MainViewModel =
        MainViewModel()

    val macaddress: String by lazy {
        intent.getStringExtra("macAddress")
    }

    private lateinit var bleDevice: RxBleDevice
    private val disconnectTriggerSubject = PublishSubject.create<Unit>()

    private lateinit var bleObservable: Observable<RxBleConnection>

    val writeUUID: UUID by lazy {
        intent.getSerializableExtra("write") as UUID
    }

    val notifyUUID: UUID by lazy {
        intent.getSerializableExtra("notify") as UUID
    }

    private var timeLeft: Int = 0
    private var countTimer: Timer? = null
    private fun countDownTask(): TimerTask = object : TimerTask() {
        override fun run() {
            timeLeft -= 1

            updateProgress()
            if (timeLeft == 10 * 60 + 30 || timeLeft == 30) {
                // 10:30 초 남았을때, 30초 남았을 때
                write(
                    RequestConverter.sendTimeRequest(timeLeft / 60, timeLeft % 60)
                )
            }
        }
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
                    }
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
//        validateConnection()
//        binding.apply {
//            ivRun.setOnClickListener {
//                write(
//                    RequestConverter.playStopRequest(!viewModel.isRunning.get())
//                )
//            }
//        }
//        settingOkTriggerSubject.subscribe {
//            write(
//                RequestConverter.setLedLevel(it.ledLevel, timeLeft / 60, timeLeft % 60)
//            )
//            write(
//                RequestConverter.setExportLevel(it.microCurrent, timeLeft / 60, timeLeft % 60)
//            )
//        }.let { compositeDisposable.add(it) }
    }

    private fun validateConnection() {
        bleDevice = BLEController.connectStream(macaddress)
        bleObservable = prepareConnectionObservable()


        bleObservable
            .flatMapSingle { it.discoverServices() }
            .flatMapSingle { it.getCharacteristic(notifyUUID) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { toast("기기에 연결중입니다.") }
            .subscribe({
                toast("기기에 연결되었습니다.")
            }, ::onConnectionError)
            .let { compositeDisposable.add(it) }

        bleObservable
            .flatMap { it.setupNotification(notifyUUID) }
            .doOnNext {
                runOnUiThread {
                    toast("기기 응답에 연결되었습니다.")

                    write(
                        RequestConverter.getAllScanRequest()
                    )
                }
            }
            .flatMap { it }
            .observeOn(AndroidSchedulers.mainThread())
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
        Log.e("Asdf", bytes.joinToString(" "))
        Log.e("Asdf", bytes.map { it.toChar() }.joinToString(" "))
        Log.e("Asdf", RequestConverter.parseStatus(bytes).toString())

        val deviceStatus = RequestConverter.parseStatus(bytes)
        updateViewModel(deviceStatus)
        // Battery NO
        if (deviceStatus?.batteryMode == 2) {
            CommonDialogFragment(
                text = "배터리 충전이 필요합니다.\n배터리를 확인해주세요.",
                _positiveCallback = {
                    startActivity<ProductSelectActivity>()
                    finish()
                    countTimer?.cancel()
                },
                _isOnlyConfirmable = true
            ).show(supportFragmentManager, "")
        }
    }

    private fun updateViewModel(deviceStatus: EcoWellStatus?) {
        deviceStatus?.run {
            viewModel.batteryLevel.set(
                when (batteryMode) {
                    1 -> BatteryLevel.FULL
                    2 -> BatteryLevel.NO
                    else -> BatteryLevel.NO
                }
            )
            viewModel.ledLevel.set(ledLevel)
            viewModel.microCurrentLevel.set(exportLevel)
            viewModel.galvanicIontoLevel.set(1)
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
        countTimer?.cancel()
        countTimer = Timer()
        countTimer?.schedule(countDownTask(), 1000, 1000)
        updateProgress()
    }

    private fun stopTimer() {
        countTimer?.cancel()
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

    private fun onConnectionError(throwable: Throwable) {
        if (throwable is BleDisconnectedException) {
            toast("블루투스 연결이 해제되었습니다.")
        } else toast("블루투스 연결 중 문제가 발생했습니다.")
        startActivity<ProductSelectActivity>()
        finish()
        countTimer?.cancel()
        throwable.printStackTrace()
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

}
