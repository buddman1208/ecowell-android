package com.buddman1208.ecowell.ui.ionstone

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.base.BaseViewModel
import com.buddman1208.ecowell.ui.luwell.BatteryLevel
import com.buddman1208.ecowell.utils.clearAndSet

class IonStoneViewModel : BaseViewModel() {

    val isKorean: ObservableBoolean = ObservableBoolean(true)

    // Datas
    val canTouchTutorial: ObservableBoolean = ObservableBoolean(true)
    val isShowTutorial: ObservableBoolean = ObservableBoolean(false)
    val batteryLevel: ObservableField<BatteryLevel> = ObservableField(
        BatteryLevel.MIDDLE
    )
    val isBluetoothEnabled: ObservableBoolean = ObservableBoolean(false)
    val progress: ObservableInt = ObservableInt(0)
    val isModeSelected: ObservableBoolean = ObservableBoolean(false)
    val isRunning: ObservableBoolean = ObservableBoolean(false)

    val currentTime: ObservableField<String> = ObservableField("")

    val modeString: ObservableField<String> = ObservableField("Mode\nSelect")
    val waterString: ObservableField<String> = ObservableField("Water\nSelect")
    val additiveString: ObservableField<String> = ObservableField("Water\nSelect")

    // Resources
    var batteryImg: ObservableInt = ObservableInt(R.drawable.img_battery_full)
    var bluetoothImg: ObservableInt = ObservableInt(R.drawable.ic_bluetooth_disable)

    init {
        val callback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                batteryImg.set(
                    when (batteryLevel.get()) {
                        BatteryLevel.LOW -> R.drawable.img_battery_low
                        BatteryLevel.MIDDLE -> R.drawable.img_battery_middle
                        BatteryLevel.FULL -> R.drawable.img_battery_full
                        else -> R.drawable.img_battery_no
                    }
                )
                bluetoothImg.set(
                    if (isBluetoothEnabled.get()) R.drawable.ic_bluetooth_enable else R.drawable.ic_bluetooth_disable
                )
            }
        }

        isBluetoothEnabled.addOnPropertyChangedCallback(callback)
        batteryLevel.addOnPropertyChangedCallback(callback)
    }

    fun onTutorialClick() {
        if (canTouchTutorial.get()) isShowTutorial.set(!isShowTutorial.get())
    }

    fun onHomepageClick() {
        browseToStart.clearAndSet("https://cellpod.co.kr/")
    }

}