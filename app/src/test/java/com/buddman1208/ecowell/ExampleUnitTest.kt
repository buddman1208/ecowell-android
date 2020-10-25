package com.buddman1208.ecowell

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        println("180 msb ${GetMSB(180)} lsb ${GetLSB(180)}")
        assertEquals(180, 0xB4)

    }

    fun GetMSB(intValue: Int): Int {
        return intValue and -0x10000
    }

    fun GetLSB(intValue: Int): Int {
        return intValue and 0x0000FFFF
    }
}

