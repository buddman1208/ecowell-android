package com.buddman1208.ecowell.utils

object IonStoneRequestConverter {

    fun getAllScanRequest(): ByteArray = listOf(0x55, 0xaa).toRequest()

    fun getStopRequest(): ByteArray = listOf(0x44, 0xaa).toRequest()

    fun getPauseRequest(leftTime: Pair<Int, Int>): ByteArray = listOf(0x33, leftTime.first, leftTime.second, 0xaa).toRequest()

    fun getPlayRequest(mode: Int): ByteArray = listOf(0x22, mode, 0xaa).toRequest()

    fun getPlayTimeSettingRequest(): ByteArray = listOf(0x11, 0x01, 0x2C, 0x01, 0xA4, 0x02, 0x1C, 0xaa).toRequest()

    fun getLeftTimeSendRequest(leftTime: Pair<Int, Int>): ByteArray = listOf(0x77, leftTime.first, leftTime.second, 0xaa).toRequest()

    private fun List<Int>.toRequest(): ByteArray {
        val sizeBeforeChecksum: Int = this.size
        val checksum = this.subList(0, sizeBeforeChecksum - 1).sum()
        return mutableListOf(
            0x55,
            sizeBeforeChecksum
        ).apply {
            addAll(this@toRequest)
            add(checksum)
        }.map {
            it.toByte()
        }.toByteArray()
    }


    fun parseNotification(byteArray: ByteArray): IonStoneStatus{
        val data = byteArray.map { String.format("%02X", it) }
        // 55,  0D,  66,  00,  03,  01,  00,  B4,  01,  2C,  01,  A4,  00,  00,  AA,  F0

        // msb = "A4".toint(16)
        // 남은 시간 msb.toInt() * 256 + lsb.toInt()
        // 상태 받아서 시작하거나 아니면 타이머 재생, 시간은 기존대로. 7분으로 고정

        val playStatus = when(data[3]) {
            "00" -> PlayStatus.WAITING
            "01" -> PlayStatus.PLAYING
            "02" -> PlayStatus.PAUSING
            "03" -> PlayStatus.COMPLETE
            "04" -> PlayStatus.REST
            else -> PlayStatus.COMPLETE
        }

        val batteryStatus = when(data[4]) {
            "00" -> BatteryStatus.NO
            "01" -> BatteryStatus.LOW
            "02" -> BatteryStatus.LEVEL1
            "03" -> BatteryStatus.LEVEL2
            else -> BatteryStatus.NO
        }

        val currentSetting = data[5].toInt()

        val leftTimeMsb = data[12].toInt(16)
        val leftTimeLsb = data[13].toInt(16)
        val leftTime = leftTimeMsb * 256 + leftTimeLsb

        return IonStoneStatus(
            playStatus = playStatus,
            batteryStatus = batteryStatus,
            currentSetting = currentSetting,
            leftTime = leftTime
        )
    }

    enum class PlayStatus {
        WAITING, PLAYING, PAUSING, COMPLETE, REST;

        fun canGetTime(): Boolean {
            return this == PLAYING || this == PAUSING
        }
    }

    enum class BatteryStatus {
        NO, LOW, LEVEL1, LEVEL2
    }
}
fun ByteArray.toStringArray() = this.map { String.format("%02X", it) }

data class IonStoneStatus(
    val playStatus: IonStoneRequestConverter.PlayStatus,
    val batteryStatus: IonStoneRequestConverter.BatteryStatus,
    val currentSetting: Int,
    val leftTime: Int
)


