package com.vitalyk.viz.scale

import java.util.Comparator

inline fun <reified R> scaleLinear(
    noinline reinterpolatorFactory: ReinterpolatorFactory<R>? = null,
    noinline deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    rangeComparator: Comparator<R>? = null,
    init: LinearScale<R>.() -> Unit = {}
): LinearScale<R> {
    val scale = LinearScale(
        reinterpolatorFactory ?: reinterpolatorFor<R>(),
        when {
            deinterpolatorFactory != null -> deinterpolatorFactory
            reinterpolatorFactory != null -> null
            else -> null
        },
        rangeComparator
    )
    scale.init()

    return scale
}

open class LinearScale<R>(
    reinterpolatorFactory: ReinterpolatorFactory<R>,
    deinterpolatorFactory: DeinterpolatorFactory<R>? = null,
    rangeComparator: Comparator<R>? = null
) : ContinuousScale<R>(reinterpolatorFactory, deinterpolatorFactory, rangeComparator) {

    override fun deinterpolatorOf(a: Double, b: Double): (Double) -> Double {
        val d = b - a
        return when {
            d == -0.0 || d == +0.0 || d.isNaN() -> { _ -> d }
            else -> { x -> (x - a) / d }
        }
    }

    override fun reinterpolatorOf(a: Double, b: Double): (Double) -> Double = reinterpolateNumber(a, b)

    fun nice(count: Int = 10): LinearScale<R> {
        val i = domain.size - 1
        val start: Double = domain.first()
        val stop: Double = domain.last()
        var step = tickStep(start, stop, count)

        if (step > 0) {
            step = tickStep(Math.floor(start / step) * step, Math.ceil(stop / step) * step, count)
            domain[0] = Math.floor(start / step) * step
            domain[i] = Math.ceil(stop / step) * step
        }

        return this
    }

    override fun ticks(count: Int) = ticks(domain.first(), domain.last(), count)
}
