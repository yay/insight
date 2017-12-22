package com.vitalyk.insight.dv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class LogInterpolatorTest {
    private val delta = 0.0000000001

    @Test
    fun toLogInterpolatorTest() {
        val interval: Interval = 10.0 to 1000.0
        val log10Interpolator = interval.toLogInterpolator(10.0)

        assertEquals(log10Interpolator(-0.5), 1.0)
        assertEquals(log10Interpolator(0.0), 10.0)
        assertEquals(log10Interpolator(0.5), 100.0)
        assertEquals(log10Interpolator(1.0), 1000.0)
        assertEquals(log10Interpolator(2.0), 100_000.0)
    }

    @Test
    fun toLogDeinterpolator() {
        val interval: Interval = 10.0 to 1000.0
        val log10Deinterpolator = interval.toLogDeinterpolator(10.0)

        assertEquals(log10Deinterpolator(1.0), -0.5)
        assertEquals(log10Deinterpolator(10.0), 0.0)
        assertEquals(log10Deinterpolator(100.0), 0.5)
        assertEquals(log10Deinterpolator(1000.0), 1.0)
        assertEquals(log10Deinterpolator(100_000.0), 2.0)
    }

    @Test
    fun logInterpolatorTest() {
        val domain = 10.0 to 1000.0
        val range = 100.0 to 200.0
        val log10Interpolator = logInterpolator(domain, range, 10.0)

        assertEquals(log10Interpolator(10.0), 100.0)
        assertEquals(log10Interpolator(100.0), 150.0)
        assertEquals(log10Interpolator(200.0), 165.05149978319906)
        assertEquals(log10Interpolator(1000.0), 200.0)
        assertEquals(log10Interpolator(10_000.0), 250.0)
    }

    @Test
    fun logDeinterpolatorTest() {
        val domain = 10.0 to 1000.0
        val range = 100.0 to 200.0
        val log10Deinterpolator = logDeinterpolator(range, domain, 10.0)

        assertEquals(log10Deinterpolator(100.0), 10.0)
        assertEquals(log10Deinterpolator(150.0), 100.0)
        assertEquals(log10Deinterpolator(165.05149978319906), 200.0, delta)
        assertEquals(log10Deinterpolator(200.0), 1000.0)
        assertEquals(log10Deinterpolator(250.0), 10_000.0)
    }
}
