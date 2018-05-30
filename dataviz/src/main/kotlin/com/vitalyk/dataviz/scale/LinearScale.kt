package com.vitalyk.dataviz.scale

import com.vitalyk.dataviz.array.tickStep
import com.vitalyk.dataviz.interpolate.*
import com.vitalyk.dataviz.array.ticks

open class LinearScale<R>(
    reinterpolatorFactory: ReinterpolatorFactory<R>,
    deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    rangeComparator: Comparator<R>? = null
) : ContinuousScale<R>(reinterpolatorFactory, deinterpolatorFactory, rangeComparator) {

    override fun deinterpolatorOf(a: Double, b: Double): Deinterpolator<Double> {
        val d = b - a
        return when {
            d == -0.0 || d == +0.0 || d.isNaN() -> { _ -> d }
            else -> { x -> (x - a) / d }
        }
    }

    override fun reinterpolatorOf(a: Double, b: Double): Reinterpolator<Double> = interpolateNumber(a, b)

    fun domain(vararg d: Double) = domain(d.toList())

    fun range(vararg r: R) = range(r.toList())

    fun nice(count: Int = 10) {
        val i = domain.size - 1
        val start: Double = domain.first()
        val stop: Double = domain.last()
        var step = tickStep(start, stop, count)

        if (step > 0) {
            step = tickStep(Math.floor(start / step) * step, Math.ceil(stop / step) * step, count)
            domain[0] = Math.floor(start / step) * step
            domain[i] = Math.ceil(stop / step) * step
        }
    }

    override fun ticks(count: Int) = ticks(domain.first(), domain.last(), count)
}