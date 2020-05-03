package com.buddman1208.ecowell.ui.main

import androidx.core.os.bundleOf
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseViewModel
import com.buddman1208.ecowell.ui.setting.SettingDialogFragment
import com.buddman1208.ecowell.utils.clearAndSet

class MainViewModel : BaseViewModel() {

    // Datas
    val isShowTutorial: ObservableBoolean = ObservableBoolean(false)
    val batteryLevel: ObservableField<BatteryLevel> = ObservableField(
        BatteryLevel.FULL
    )
    val ledLevel: ObservableInt = ObservableInt(0)
    val microCurrentLevel: ObservableInt = ObservableInt(0)
    val galvanicIontoLevel: ObservableInt = ObservableInt(0)
    val progress: ObservableInt = ObservableInt(75)
    val isRunning: ObservableBoolean = ObservableBoolean(false)


    // Resources
    var batteryImg: ObservableInt = ObservableInt(R.drawable.img_battery_full)
    var ledImg: ObservableInt = ObservableInt(R.drawable.btn_led_off)
    var microImg: ObservableInt = ObservableInt(R.drawable.btn_microcurrent_off)
    var galvanicImg: ObservableInt = ObservableInt(R.drawable.btn_galvanic_off)

    init {
        val callback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                batteryImg.set(
                    when (batteryLevel.get()) {
                        BatteryLevel.NO -> R.drawable.img_battery_no
                        BatteryLevel.LOW -> R.drawable.img_battery_low
                        BatteryLevel.FULL -> R.drawable.img_battery_full
                        else -> R.drawable.img_battery_no
                    }
                )
                ledImg.set(
                    when (ledLevel.get()) {
                        0 -> R.drawable.btn_led_off
                        1 -> R.drawable.btn_led_1
                        2 -> R.drawable.btn_led_2
                        3 -> R.drawable.btn_led_3
                        else -> R.drawable.btn_led_off
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
                        else -> R.drawable.btn_microcurrent_off
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

        batteryLevel.addOnPropertyChangedCallback(callback)
        ledLevel.addOnPropertyChangedCallback(callback)
        microCurrentLevel.addOnPropertyChangedCallback(callback)
        galvanicIontoLevel.addOnPropertyChangedCallback(callback)
    }

    fun onTutorialClick() {
        isShowTutorial.set(!isShowTutorial.get())
    }

    fun onLedClick() {
        val level = ledLevel.get()
        ledLevel.set(
            if (level < 3) level + 1 else 0
        )
    }

    fun onMicrocurrentClick() {
        val level = microCurrentLevel.get()
        microCurrentLevel.set(
            if (level < 5) level + 1 else 0
        )
    }

    fun onGalvanicClick() {
        val level = galvanicIontoLevel.get()
        galvanicIontoLevel.set(
            if (level < 1) level + 1 else 0
        )
    }

    fun onHomepageClick() {
        browseToStart.clearAndSet("http://ecowell.co.kr/")
    }

    fun onSettingClick() {
        dialogToStart.clearAndSet(Pair(SettingDialogFragment::class, bundleOf()))
    }

}

enum class BatteryLevel {
    NO, LOW, FULL
}