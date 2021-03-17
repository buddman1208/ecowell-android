package com.buddman1208.ecowell.ui.ionstone

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityIonstoneBinding
import com.buddman1208.ecowell.ui.base.BaseActivity
import com.buddman1208.ecowell.ui.commondialog.CommonDialogFragment
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
import kotlinx.android.synthetic.main.activity_luwell.*
import kotlinx.android.synthetic.main.activity_test.*
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

    private var maxTime = MIN_9
    private var timeLeft: Int = 0
    private var handler: Handler? = Handler()
    private var checkHandler: Handler? = Handler()

    var time = System.currentTimeMillis()

//    private fun timerRunnable(): Runnable = Runnable {
//        handler?.postDelayed(timerRunnable(), 1000)
//        timeLeft -= 1
//        time = System.currentTimeMillis()
//
//        updateProgress()
//        write(
//            IonStoneRequestConverter.getLeftTimeSendRequest(
//                Pair(timeLeft.getMsb(), timeLeft.getLsb())
//            )
//        )
//    }

    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long? = null
    private fun getTimerInstance(): CountDownTimer {
        return object : CountDownTimer(timeLeftInMillis ?: timeLeft * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                timeLeft = (millisUntilFinished / 1000).toInt()

                updateProgress()
                write(
                    IonStoneRequestConverter.getLeftTimeSendRequest(
                        Pair(timeLeft.getMsb(), timeLeft.getLsb())
                    )
                )
            }

            override fun onFinish() {
                timeLeftInMillis = null
                timeLeft = 0
            }
        }
    }


    private fun restRunnable(): Runnable = Runnable {
        handler?.postDelayed(restRunnable(), 500)
        write(
            IonStoneRequestConverter.getAllScanRequest()
        )
    }


    private val eventCallback =
        object : androidx.databinding.Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(
                sender: androidx.databinding.Observable?,
                propertyId: Int
            ) {
                viewModel.setStringByMode()
                val leftTime = when (viewModel.mode.get()) {
                    1 -> MIN_5
                    2 -> MIN_7
                    3 -> MIN_9
                    else -> MIN_9
                }

                maxTime = leftTime
                timeLeft = leftTime
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
                if (viewModel.mode.get() != 0) {
                    if (!viewModel.isModeSelected.get()) {
                        write(IonStoneRequestConverter.getPlayTimeSettingRequest())
                        viewModel.isModeSelected.set(true)
                    }
                    val time = timeLeft
                    if (viewModel.isRunning.get()) {
                        write(
                            IonStoneRequestConverter.getPauseRequest(
                                Pair(time.getMsb(), time.getLsb())
                            )
                        )
                    } else {
                        write(IonStoneRequestConverter.getPlayRequest(viewModel.mode.get()))
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
                    viewModel.changeMode()

            }
            ivWater.setOnClickListener {
                if (!viewModel.isModeSelected.get())
                    viewModel.changeMode()
            }
            ivAdditives.setOnClickListener {
                if (!viewModel.isModeSelected.get())
                    viewModel.changeMode()
            }
        }
//        test()
    }

    private fun test() {
        viewModel.mode.set(3)
        maxTime = MIN_7
        timeLeft = MIN_7
        startTimer()
        ivRun.setOnClickListener {
            if (isCountDownTimerRunning) stopTimer()
            else startTimer()
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
        try {
            // "X7L:2000078N"
            bleObservable
                .firstOrError()
                .flatMap { it.writeCharacteristic(writeUUID, writeTarget) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.e("asdf", "write success ${it.toStringArray().joinToString(", ")}")
                }, ::onConnectionError)
                .let { compositeDisposable.add(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
    var previousMode: IonStoneRequestConverter.PlayStatus =
        IonStoneRequestConverter.PlayStatus.WAITING

    private fun updateViewModel(deviceStatus: IonStoneStatus) {
        if (viewModel.isModeSelected.get().not()) {
            if (deviceStatus.playStatus != IonStoneRequestConverter.PlayStatus.WAITING) {
                viewModel.isModeSelected.set(true)
                viewModel.mode.set(deviceStatus.currentSetting)
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
        viewModel.isResting.set(deviceStatus.playStatus == IonStoneRequestConverter.PlayStatus.REST)
        when (deviceStatus.playStatus) {
            IonStoneRequestConverter.PlayStatus.REST -> {
                if (deviceStatus.leftTime <= 0) {
                    showDialogAndFinish(MainBluetoothState.REST_COMPLETE)
                    compositeDisposable.clear()
                    handler?.removeCallbacksAndMessages(null)
                    return
                }
                maxTime = MIN_1
                if (previousMode == IonStoneRequestConverter.PlayStatus.PLAYING) {
                    onOperationCompleteDialog()
                }
                stopTimer()
                handler?.postDelayed(restRunnable(), 500)
            }
            IonStoneRequestConverter.PlayStatus.PLAYING -> {
                viewModel.isRunning.set(true)
                startTimer()
            }
            else -> {
                viewModel.isRunning.set(false)
                stopTimer()
            }
        }

        if (deviceStatus.playStatus == IonStoneRequestConverter.PlayStatus.REST || !isTimeReceived || !deviceStatus.playStatus.canGetTime()) {
            Log.e("asdf", "updating time $isTimeReceived ${deviceStatus.playStatus}")

            updateTimer(deviceStatus.leftTime)
            if (deviceStatus.playStatus.canGetTime()) {
                isTimeReceived = true
            }
        }

        previousMode = deviceStatus.playStatus
    }

    private fun getBatteryStatus(batteryLevel: Int): BatteryLevel {
        return when (batteryLevel) {
            0 -> BatteryLevel.NO
            1 -> BatteryLevel.LOW
            2 -> BatteryLevel.MIDDLE
            else -> BatteryLevel.FULL
        }
    }

    private var isCompleteOrErrorOccurred: Boolean = false
    private fun showDialogAndFinish(state: MainBluetoothState) {
        try {
            if (isCompleteOrErrorOccurred.not()) {
                compositeDisposable.clear()
                handler?.removeCallbacksAndMessages(null)
                isCompleteOrErrorOccurred = true
                viewModel.isBluetoothEnabled.set(false)
                disconnectTriggerSubject.onNext("")
                CommonDialogFragment(
                    text = when (state) {
                        MainBluetoothState.COMPLETE -> resources.getString(R.string.operation_completed)
                        MainBluetoothState.REST_COMPLETE -> resources.getString(R.string.rest_completed)
                        MainBluetoothState.BLUETOOTH_ERROR -> resources.getString(R.string.bluetooth_error)
                        MainBluetoothState.BLUETOOTH_DISCONNECTED -> resources.getString(R.string.bluetooth_disconnected)
                        MainBluetoothState.LOW_BATTERY -> resources.getString(R.string.low_battery)
                    },
                    _positiveCallback = {
                        startActivity<ProductSelectActivity>()
                        finish()
                        handler?.removeCallbacksAndMessages(null)
                    },
                    _isOnlyConfirmable = true,
                    _isCancelable = false
                ).show(supportFragmentManager, "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onOperationCompleteDialog() {
        CommonDialogFragment(
            text = resources.getString(R.string.operation_completed),
            _positiveCallback = {},
            _isOnlyConfirmable = true,
            _isCancelable = false
        ).show(supportFragmentManager, "")
    }

    private fun updateTimer(leftSeconds: Int = MIN_7) {
        timeLeft = leftSeconds
        updateProgress()
    }

    private var isCountDownTimerRunning: Boolean = false
    private fun startTimer() {
//        handler?.removeCallbacksAndMessages(null)
//        handler?.postDelayed(timerRunnable(), 1000)
        timer = getTimerInstance()
        timer?.start()
        updateProgress()
        isCountDownTimerRunning = true
    }

    private fun stopTimer() {
//        handler?.removeCallbacksAndMessages(null)
        timer?.cancel()
        updateProgress()
        isCountDownTimerRunning = false
    }

    private fun updateProgress() {
        viewModel.currentTime.set(
            "${String.format(
                "%02d",
                timeLeft / 60
            )} : ${String.format("%02d", timeLeft % 60)}"
        )
        val progress = timeLeft * 100 / maxTime
        viewModel.progress.set(progress)
    }

    private fun onConnectionError(throwable: Throwable) {
        if (throwable is BleDisconnectedException) {
            showDialogAndFinish(MainBluetoothState.BLUETOOTH_DISCONNECTED)
        } else showDialogAndFinish(MainBluetoothState.BLUETOOTH_ERROR)
    }

    override fun onPause() {
        super.onPause()
        viewModel.mode.removeOnPropertyChangedCallback(eventCallback)
    }

    override fun onResume() {
        super.onResume()
        viewModel.mode.addOnPropertyChangedCallback(eventCallback)
    }

    companion object {
        const val MIN_9: Int = 9 * 60
        const val MIN_7: Int = 7 * 60
        const val MIN_5: Int = 5 * 60
        const val MIN_1: Int = 1 * 60
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
    LOW_BATTERY,
    REST_COMPLETE
}