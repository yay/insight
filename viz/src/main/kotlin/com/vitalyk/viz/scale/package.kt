package com.vitalyk.viz.scale

import kotlin.reflect.full.isSubclassOf

typealias Reinterpolator<T> = (Double) -> T
typealias Deinterpolator<T> = (T) -> Double

typealias ReinterpolatorFactory<T> = (T, T) -> Reinterpolator<T>
typealias DeinterpolatorFactory<T> = (T, T) -> Deinterpolator<T>

typealias PiecewiseReinterpolatorFactory<T> = (List<Double>, List<T>, DeinterpolatorFactory<Double>, ReinterpolatorFactory<T>) -> Reinterpolator<T>
typealias PiecewiseDeinterpolatorFactory<T> = (List<T>, List<Double>, DeinterpolatorFactory<T>, ReinterpolatorFactory<Double>) -> Deinterpolator<T>


inline fun <reified R> reinterpolatorFor() = when {
    R::class.isSubclassOf(Number::class) -> ::reinterpolateNumber
//    R::class == Color::class -> ::interpolateRgb
    else -> throw IllegalArgumentException()
} as ReinterpolatorFactory<R>

inline fun <reified R> deinterpolatorFor(): DeinterpolatorFactory<R>? {
    return when {
        R::class.isSubclassOf(Number::class) -> { a: R, b: R ->
            val a = (a as Number).toDouble()
            val b = (b as Number).toDouble()
            val d = b - a
            when {
                d == -0.0 || d == +0.0 || d.isNaN() -> { _ -> d }
                else -> { x: R -> ((x as Number).toDouble() - a) / d }
            }
        }
        else -> null
    }
}

fun main(args: Array<String>) {
    val scale = scaleLinear<Int> {
        domain(0.0, 5.0)
        range(0, 100)
    }
    val asfsdf = scale(2.503)
    println(asfsdf)
}