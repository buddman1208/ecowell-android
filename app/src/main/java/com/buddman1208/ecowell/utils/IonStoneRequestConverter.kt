package com.buddman1208.ecowell.utils

object IonStoneRequestConverter {

    fun getAllScanRequest(): ByteArray = listOf(0x55, 0xaa).toRequest()

    fun getStopRequest(): ByteArray = listOf(0x44, 0xaa).toRequest()

    fun getPauseRequest(leftTime: Pair<Int, Int>): ByteArray = listOf(0x33, leftTime.first, leftTime.second, 0xaa).toRequest()

    fun getPlayRequest(mode: Int): ByteArray = listOf(0x22, mode, 0xaa).toRequest()

    fun getPlayTimeSettingRequest(): ByteArray = listOf(0x11, 0x00, 0xb4, 0x01, 0x2c, 0x01, 0xa4, 0xaa).toRequest()

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


    fun parseNotification(byteArray: ByteArray) {
        val data = byteArray.map { String.format("%02X", it) }
        // 55,  0D,  66,  00,  03,  01,  00,  B4,  01,  2C,  01,  A4,  00,  00,  AA,  F0

        // msb = "A4".toint(16)
        // 남은 시간 msb.toInt() * 256 + lsb.toInt()
        // 상태 받아서 시작하거나 아니면 타이머 재생, 시간은 기존대로. 7분으로 고정

    }

    enum class PlayStatus {
        WAITING, PLAYING, PAUSING, COMPLETE, REST
    }

    enum class BatteryStatus {
        NO, LOW, LEVEL1, LEVEL2, LEVEL3
    }
}
fun ByteArray.toStringArray() = this.map { String.format("%02X", it) }


