package com.vitalyk.dataviz.interpolate

typealias Reinterpolator<T> = (Double) -> T
typealias Deinterpolator<T> = (T) -> Double

typealias ReinterpolatorFactory<T> = (T, T) -> Reinterpolator<T>
typealias DeinterpolatorFactory<T> = (T, T) -> Deinterpolator<T>

typealias PiecewiseReinterpolatorFactory<T> =
    (List<Double>, List<T>, DeinterpolatorFactory<Double>, ReinterpolatorFactory<T>) -> Reinterpolator<T>
typealias PiecewiseDeinterpolatorFactory<T> =
    (List<T>, List<Double>, DeinterpolatorFactory<T>, ReinterpolatorFactory<Double>) -> Deinterpolator<T>


fun interpolateNumber(a: Double, b: Double): Reinterpolator<Double> {
    val d = b - a
    return { t -> a + d * t }
}

fun interpolateRound(a: Double, b: Double): Reinterpolator<Double> {
    val d = b - a
    return { t -> Math.round(a + d * t).toDouble() }
}