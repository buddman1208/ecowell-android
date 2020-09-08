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
import com.buddman1208.ecowell.utils.SettingCache
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.startActivity
import java.util.*

class IonStoneActivity : BaseActivity<ActivityIonstoneBinding, IonStoneViewModel>(
    R.layout.activity_ionstone
) {
    public override val viewModel: IonStoneViewModel =
        IonStoneViewModel()

    private var timeLeft: Int = 0
    private var countTimer: Timer? = null
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


        binding.apply {
            ivRun.setOnClickListener {
                if (viewModel.isModeSelected.get()) {
                    if (viewModel.isRunning.get()) {
                        stopTimer()
                        viewModel.isRunning.set(false)
                    } else {
                        startTimer()
                        viewModel.isRunning.set(true)
                    }
                }
            }
            ivMode.setOnClickListener {
                openDialog()
            }
            ivWater.setOnClickListener {
                openDialog()
            }
            ivAdditives.setOnClickListener {
                openDialog()
            }
        }
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