package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpeedHistoryTest {

    @Test
    fun append_toEmptyHistory_returnsSingleSample() {
        val sample = SpeedSample(10.0, 20.0)

        val result = appendSpeedSample(emptyList(), sample)

        assertEquals(listOf(sample), result)
    }

    @Test
    fun append_belowCap_keepsOrder() {
        val first = SpeedSample(1.0, 2.0)
        val second = SpeedSample(3.0, 4.0)

        val result = appendSpeedSample(listOf(first), second, maxSamples = 3)

        assertEquals(listOf(first, second), result)
    }

    @Test
    fun append_atCap_dropsOldest() {
        val samples = (1..5).map { SpeedSample(it.toDouble(), it.toDouble()) }

        var history = emptyList<SpeedSample>()
        samples.forEach { history = appendSpeedSample(history, it, maxSamples = 3) }

        assertEquals(samples.drop(2), history)
    }

    @Test
    fun append_nonPositiveCap_returnsEmpty() {
        val result = appendSpeedSample(listOf(SpeedSample(1.0, 1.0)), SpeedSample(2.0, 2.0), maxSamples = 0)

        assertTrue(result.isEmpty())
    }
}
