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

}
fun ByteArray.toStringArray() = this.map { String.format("%02X", it) }


