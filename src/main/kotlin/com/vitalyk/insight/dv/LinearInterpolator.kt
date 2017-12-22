package com.vitalyk.insight.dv

fun Interval.toLinearInterpolator(): (t: Double) -> Double = { t ->
    this.first * (1 - t) + this.second * t
}

fun Interval.toLinearDeinterpolator(): (x: Double) -> Double = { x ->
    (x - this.first) / (this.second - this.first)
}

fun linearInterpolator(from: Interval, to: Interval): (x: Double) -> Double {
    val deinterpolator = from.toLinearDeinterpolator()
    val interpolator = to.toLinearInterpolator()

    return { x -> interpolator(deinterpolator(x)) }
}