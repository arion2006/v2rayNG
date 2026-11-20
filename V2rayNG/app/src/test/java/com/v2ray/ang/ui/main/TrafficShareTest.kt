package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrafficShareTest {

    @Test
    fun uplinkShare_noTraffic_isZero() {
        assertEquals(0f, uplinkShare(0L, 0L))
    }

    @Test
    fun uplinkShare_onlyUplink_isOne() {
        assertEquals(1f, uplinkShare(100L, 0L))
    }

    @Test
    fun uplinkShare_onlyDownlink_isZero() {
        assertEquals(0f, uplinkShare(0L, 100L))
    }

    @Test
    fun uplinkShare_quarterUplink() {
        assertEquals(0.25f, uplinkShare(25L, 75L))
    }
}
