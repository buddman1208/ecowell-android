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
import com.buddman1208.ecowell.ui.luwell.BatteryLevel
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import com.buddman1208.ecowell.ui.setting.SettingDialogFragment
import com.buddman1208.ecowell.utils.*
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
    private var checkHandler: Handler? = Handler()

    var time = System.currentTimeMillis()

    private fun timerRunnable(): Runnable = Runnable {
        handler?.postDelayed(timerRunnable(), 1000)
        timeLeft -= 1
        time = System.currentTimeMillis()

        updateProgress()
        write(
            IonStoneRequestConverter.getLeftTimeSendRequest(
                Pair(timeLeft.getMsb(), timeLeft.getLsb())
            )
        )
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
                if (viewModel.isModeSelected.get()) {
                    val time = timeLeft
                    if (viewModel.isRunning.get()) {
                        write(
                            IonStoneRequestConverter.getPauseRequest(
                                Pair(time.getMsb(), time.getLsb())
                            )
                        )
                    } else {
                        write(IonStoneRequestConverter.getPlayRequest(3))
                        Handler().postDelayed({
                            write(
                                IonStoneRequestConverter.getLeftTimeSendRequest(
                                    Pair(timeLeft.getMsb(), timeLeft.getLsb())
                                )
                            )
                        }, 100)
                    }
                }
            }
            ivMode.setOnClickListener {
                if (!viewModel.isModeSelected.get())
                    openDialog()

            }
            ivWater.setOnClickListener {
                if (!viewModel.isModeSelected.get())
                    openDialog()
            }
            ivAdditives.setOnClickListener {
                if (!viewModel.isModeSelected.get())
                    openDialog()
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
//                    checkHandler?.postDelayed(checkRunnable(), 500)
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
        val byteStrings = bytes.map { String.format("%02X", it) }
        Log.e("asdf", byteStrings.joinToString(",  "))

        val parsed = IonStoneRequestConverter.parseNotification(bytes)
        println(parsed)

        updateViewModel(parsed)
    }

    var isTimeReceived: Boolean = false
    private fun updateViewModel(deviceStatus: IonStoneStatus) {
        if (viewModel.isModeSelected.get().not()) {
            if (deviceStatus.playStatus != IonStoneRequestConverter.PlayStatus.WAITING) {
                viewModel.isModeSelected.set(true)
            }
        }
        viewModel.batteryLevel.set(
            getBatteryStatus(
                when (deviceStatus.batteryStatus) {
                    IonStoneRequestConverter.BatteryStatus.NO -> 0
                    IonStoneRequestConverter.BatteryStatus.LOW -> 1
                    IonStoneRequestConverter.BatteryStatus.LEVEL1 -> 2
                    IonStoneRequestConverter.BatteryStatus.LEVEL2 -> 3
                }
            )
        )
        if (deviceStatus.playStatus == IonStoneRequestConverter.PlayStatus.PLAYING) {
            viewModel.isRunning.set(true)
            startTimer()
        } else {
            viewModel.isRunning.set(false)
            stopTimer()
        }

        if (!isTimeReceived || !deviceStatus.playStatus.canGetTime()) {
            Log.e("asdf", "updating time $isTimeReceived ${deviceStatus.playStatus}")

            updateTimer(deviceStatus.leftTime)
            if (deviceStatus.playStatus.canGetTime()) {
                isTimeReceived = true
            }
        }
    }

    private fun getBatteryStatus(batteryLevel: Int): BatteryLevel {
        return when (batteryLevel) {
            0 -> BatteryLevel.NO
            1 -> BatteryLevel.LOW
            2 -> BatteryLevel.MIDDLE
            else -> BatteryLevel.FULL
        }
    }

    private fun openDialog() {
        IonStoneSettingFragment(object : IonStoneSettingCompleteListener {
            override fun onComplete(response: IonStoneSettingResponse) {
//                viewModel.isModeSelected.set(true)
//                updateTimer()
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

                write(IonStoneRequestConverter.getPlayTimeSettingRequest(Pair(23.getMsb(), 23.getLsb())))
                Handler().postDelayed({
                    write(IonStoneRequestConverter.getPlayRequest(3))
                }, 100)
            }
        }).show(supportFragmentManager, "")
    }

    private fun showDialogAndFinish(state: MainBluetoothState) {
        viewModel.isBluetoothEnabled.set(false)
        disconnectTriggerSubject.onNext("")
        compositeDisposable.clear()
        finish()
    }

    private fun updateTimer(leftSeconds: Int = MIN_7) {
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
        val progress = timeLeft * 100 / MIN_7
        viewModel.progress.set(progress)
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
        const val MIN_7: Int = 7 * 60
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