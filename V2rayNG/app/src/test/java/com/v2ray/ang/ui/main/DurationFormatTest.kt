package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DurationFormatTest {

    @Test
    fun format_zero_isZeroMinutes() {
        assertEquals("0:00", formatDurationHms(0L))
    }

    @Test
    fun format_negative_clampsToZero() {
        assertEquals("0:00", formatDurationHms(-5_000L))
    }

    @Test
    fun format_underHour_isMinutesSeconds() {
        assertEquals("9:05", formatDurationHms(545_000L))
    }

    @Test
    fun format_overHour_includesHours() {
        assertEquals("2:03:07", formatDurationHms(7_387_000L))
    }

    @Test
    fun format_subSecond_truncates() {
        assertEquals("0:00", formatDurationHms(999L))
    }
}
