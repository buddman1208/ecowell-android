package com.buddman1208.ecowell.utils

import kotlin.math.min

object RequestConverter {

    const val format = "%1s%2sN"

    fun getAllScanRequest(): String {
        val data = "X7S:10000"
        return data.toRequest()
    }

    fun playStopRequest(requestStart: Boolean): String {
        val data = "X7P:${if (requestStart) 1 else 2}0000"
        return data.toRequest()
    }

    fun setExportLevel(exportLevel: Int, _minute: Int, _second: Int): String {
        val minute = String.format("%02d", min(20, _minute))
        val second = String.format("%02d", min(59, _second))
        val level = min(exportLevel, 5)
        val data = "X7O:${level}${minute}${second}}"
        return data.toRequest()
    }

    fun setLedLevel(ledLevel: Int, _minute: Int, _second: Int): String {
        val minute = String.format("%02d", min(20, _minute))
        val second = String.format("%02d", min(59, _second))
        val level = min(ledLevel, 5)
        val data = "X7L:${level}${minute}${second}"
        return data.toRequest()
    }

    fun setRunningTimeLevel(runningTimeLevel: Int): String {
        val level = min(runningTimeLevel, 10)
        val hexLevel = "%x".format(level)
        val data = "X7T:${hexLevel}0000"
        return data.toRequest()
    }

    fun setMode(runMode: Int): String {
        val mode = min(runMode, 3)
        val data = "X7M:${mode}0000"
        return data.toRequest()
    }

    fun setBatteryStatus(batteryMode: Int): String {
        val mode = if (batteryMode in (1..2)) batteryMode else 1
        val data = "X7B:${batteryMode}0000"
        return data.toRequest()
    }

    fun setParameterSaveEnabled(isEnabled: Boolean): String {
        val enabled = if(isEnabled) 1 else 2
        val data = "X7D:${enabled}0000"
        return data.toRequest()
    }

    fun sendTimeRequest(_minute: Int, _second: Int): String {
        val minute = String.format("%02d", min(20, _minute))
        val second = String.format("%02d", min(59, _second))
        val data = "X7A:${minute}${second}0"
        return data.toRequest()
    }

    fun parseStatus(returnValue: ByteArray): EcoWellStatus? {
        if(returnValue.size > 4) {
            val result = returnValue.map { it.toChar().toString() }.subList(3, returnValue.size)
            val isParsable = result.size >= 8
            val isTimeServed = isParsable && result.size == 12
            return EcoWellStatus(
                isRunning = when(result[1]) {
                    "1" -> true
                    "2" -> false
                    else -> false
                },
                exportLevel = result[2].toIntOrNull(16) ?: -1,
                ledLevel = result[3].toIntOrNull(16) ?: -1,
                runningTimeLevel = result[4].toIntOrNull(16) ?: -1,
                runMode = result[5].toIntOrNull(16) ?: -1,
                batteryMode = result[6].toIntOrNull(16) ?: -1,
                minute = if(isTimeServed) {
                    (result[8] + result[9]).toIntOrNull() ?: -1
                } else -1,
                second = if(isTimeServed) {
                    (result[10] + result[11]).toIntOrNull() ?: -1
                } else -1
            )
        } else return null

    }
}

fun String.checksum(): String {
    return if (length < 3) "" else String.format(
        "%02X",
        this.substring(2, length).toCharArray().map { it.toByte() }.sum().toByte()
    )
}

fun String.toRequest(): String = String.format(RequestConverter.format, this, checksum())

data class EcoWellStatus(
    var isRunning: Boolean,
    var exportLevel: Int,
    var ledLevel: Int,
    var runningTimeLevel: Int,
    var runMode: Int,
    var batteryMode: Int,
    var minute: Int,
    var second: Int
)