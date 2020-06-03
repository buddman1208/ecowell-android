package com.buddman1208.ecowell.ui.main

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.*
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseViewModel
import com.buddman1208.ecowell.utils.clearAndSet

class MainViewModel : BaseViewModel() {

    // Datas
    val isShowTutorial: ObservableBoolean = ObservableBoolean(false)
    val batteryLevel: ObservableField<BatteryLevel> = ObservableField(
        BatteryLevel.FULL
    )
    val isBluetoothEnabled: ObservableBoolean = ObservableBoolean(false)
    val ledLevel: ObservableInt = ObservableInt(0)
    val microCurrentLevel: ObservableInt = ObservableInt(0)
    val galvanicIontoLevel: ObservableInt = ObservableInt(0)
    val progress: ObservableInt = ObservableInt(0)
    val isRunning: ObservableBoolean = ObservableBoolean(false)

    val currentTime: ObservableField<String> = ObservableField("")


    // Resources
    var batteryImg: ObservableInt = ObservableInt(R.drawable.img_battery_full)
    var bluetoothImg: ObservableInt = ObservableInt(R.drawable.ic_bluetooth_disable)
    var ledImg: ObservableInt = ObservableInt(R.drawable.btn_led_off)
    var microImg: ObservableInt = ObservableInt(R.drawable.btn_microcurrent_off)
    var galvanicImg: ObservableInt = ObservableInt(R.drawable.btn_galvanic_off)

    init {
        val callback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                batteryImg.set(
                    when (batteryLevel.get()) {
                        BatteryLevel.LOW -> R.drawable.img_battery_low
                        BatteryLevel.FULL -> R.drawable.img_battery_full
                        else -> R.drawable.img_battery_no
                    }
                )
                bluetoothImg.set(
                    if (isBluetoothEnabled.get()) R.drawable.ic_bluetooth_enable else R.drawable.ic_bluetooth_disable
                )
                ledImg.set(
                    when (ledLevel.get()) {
                        0 -> R.drawable.btn_led_off
                        1 -> R.drawable.btn_led_1
                        2 -> R.drawable.btn_led_2
                        3 -> R.drawable.btn_led_3
                        else -> R.drawable.btn_led_3
                    }
                )
                microImg.set(
                    when (microCurrentLevel.get()) {
                        0 -> R.drawable.btn_microcurrent_off
                        1 -> R.drawable.btn_microcurrent_1
                        2 -> R.drawable.btn_microcurrent_2
                        3 -> R.drawable.btn_microcurrent_3
                        4 -> R.drawable.btn_microcurrent_4
                        5 -> R.drawable.btn_microcurrent_5
                        else -> R.drawable.btn_microcurrent_5
                    }
                )
                galvanicImg.set(
                    when (galvanicIontoLevel.get()) {
                        0 -> R.drawable.btn_galvanic_off
                        1 -> R.drawable.btn_galvanic_1
                        else -> R.drawable.btn_galvanic_off
                    }
                )
            }
        }

        isBluetoothEnabled.addOnPropertyChangedCallback(callback)
        batteryLevel.addOnPropertyChangedCallback(callback)
        ledLevel.addOnPropertyChangedCallback(callback)
        microCurrentLevel.addOnPropertyChangedCallback(callback)
        galvanicIontoLevel.addOnPropertyChangedCallback(callback)
    }

    fun onTutorialClick() {
        isShowTutorial.set(!isShowTutorial.get())
    }

    fun onLedClick() {
//        val level = ledLevel.get()
//        ledLevel.set(
//            if (level < 3) level + 1 else 0
//        )
    }

    fun onMicrocurrentClick() {
//        val level = microCurrentLevel.get()
//        microCurrentLevel.set(
//            if (level < 5) level + 1 else 0
//        )
    }

    fun onGalvanicClick() {
//        val level = galvanicIontoLevel.get()
//        galvanicIontoLevel.set(
//            if (level < 1) level + 1 else 0
//        )
    }

    fun onHomepageClick() {
        browseToStart.clearAndSet("https://cellpod.co.kr/")
    }

    fun onSettingClick() {
//        dialogToStart.clearAndSet(Pair(SettingDialogFragment::class, bundleOf()))
        event.clearAndSet("openSetting")
    }

    companion object {
        @JvmStatic
        @BindingAdapter("rightImg")
        fun setRightImage(view: TextView, res: ObservableInt) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(view.context, res.get())
                , null
            )
        }
    }
}

enum class BatteryLevel {
    NO, LOW, FULL
}