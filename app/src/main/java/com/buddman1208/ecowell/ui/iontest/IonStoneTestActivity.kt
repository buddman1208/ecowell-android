package com.buddman1208.ecowell.ui.iontest

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import com.buddman1208.ecowell.utils.BLEController
import com.buddman1208.ecowell.utils.IonStoneRequestConverter
import com.buddman1208.ecowell.utils.toStringArray
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_ion_stone_test.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class IonStoneTestActivity : AppCompatActivity() {

    val compositeDisposable: CompositeDisposable = CompositeDisposable()
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
        write(
            IonStoneRequestConverter.getPlayTimeSettingRequest()
        )
        handler?.postDelayed(timerRunnable(), 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ion_stone_test)

        validateConnection()
        disconnectTriggerSubject
            .subscribe { compositeDisposable.clear() }
            .let { compositeDisposable.add(it) }


        test()

    }


    private fun test() {
        cbSendSetting.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                handler?.removeCallbacksAndMessages(null)
                handler?.postDelayed(timerRunnable(), 1000)
            } else {
                handler?.removeCallbacksAndMessages(null)
            }
        }

        btnRequestStatus.setOnClickListener {
            write(
                IonStoneRequestConverter.getAllScanRequest()
            )
        }

        btnStart.setOnClickListener {
            val mode = when (rgStartMode.checkedRadioButtonId) {
                R.id.startMode1 -> 0x01
                R.id.startMode2 -> 0x02
                R.id.startMode3 -> 0x03
                else -> 0x01
            }
            write(
                IonStoneRequestConverter.getPlayRequest(mode)
            )
        }

        btnPause.setOnClickListener {
            val leftTime = when (rgLeftTime.checkedRadioButtonId) {
                R.id.leftTime1 -> Pair(0x02, 0x58)
                R.id.leftTime2 -> Pair(0x01, 0x2c)
                R.id.leftTime3 -> Pair(0x00, 0x0a)
                else -> Pair(0x02, 0x58)
            }
            write(
                IonStoneRequestConverter.getPauseRequest(leftTime)
            )
        }

        btnStop.setOnClickListener {
            write(
                IonStoneRequestConverter.getStopRequest()
            )
        }

        btnSetRunningTime.setOnClickListener {
            write(
                IonStoneRequestConverter.getPlayTimeSettingRequest()
            )
        }
    }

    private fun updateLastUpdateStatus(value: String) {
        tvStatus.text = String.format(
            getString(R.string.last_received_notification),
            value
        )
    }

    private fun updateLastSentValue(value: String) {
        tvLastSent.text = String.format(
            getString(R.string.last_sent_value),
            value
        )
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
        bleObservable
            .firstOrError()
            .flatMap { it.writeCharacteristic(writeUUID, writeTarget) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.e("asdf", "write success ${it.toStringArray()}")
                updateLastSentValue(
                    it.toStringArray().joinToString(", ")
                )
            }, ::onConnectionError)
            .let { compositeDisposable.add(it) }
    }

    private fun onConnectionError(throwable: Throwable) {
        throwable.printStackTrace()
        runOnUiThread {
            onBackPressed()
        }
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
        bleDevice
            .establishConnection(true)
            .takeUntil(disconnectTriggerSubject)
            .compose(ReplayingShare.instance())

    private fun onNotificationReceived(bytes: ByteArray) {
        val received = bytes.toStringArray().joinToString(", ")

        Log.e("asdf", received)

        updateLastUpdateStatus(received)
    }

    override fun onBackPressed() {
        handler?.removeCallbacksAndMessages(null)
        disconnectTriggerSubject.onNext("")
        startActivity<ProductSelectActivity>()
        finish()
    }

}