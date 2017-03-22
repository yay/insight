package dv

fun Interval.toInterpolator(): (t: Double) -> Double = {
    t -> this.first * (1 - t) + this.second * t
}

fun Interval.toDeinterpolator(): (x: Double) -> Double = {
    x -> (x - this.first) / (this.second - this.first)
}

fun interpolate(from: Interval, to: Interval): (x: Double) -> Double {
    val deinterpolator = from.toDeinterpolator()
    val interpolator = to.toInterpolator()

    return { x -> interpolator(deinterpolator(x)) }
}

infix fun Interval.to(that: Interval) = interpolate(this, that)