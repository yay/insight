package dv

fun log(x: Double, base: Double): Double {
    return when (base) {
        Math.E -> Math.log(x)
        10.0 -> Math.log10(x)
        else -> Math.log(x) / Math.log(base)
    }
}

/**
 * Returns a function that goes from a value in the [0, 1] interval to a value in the
 * given interval.
 *
 * The log of the given interval is taken first to produce the interval for the exponent,
 * which is the interval that actually gets interpolated. The `base` is then raised to
 * the power of the resulting exponent.
 */
fun Interval.toLogInterpolator(base: Double): (t: Double) -> Double {
    val sign = Math.signum(this.first)
    val otherSign = Math.signum(this.second)

    if (sign != otherSign) {
        throw Exception("The interval should be strictly positive or strictly negative.")
    }

    val log0 = log(this.first * sign, base)
    val log1 = log(this.second * sign, base)

    val interpolator = (log0 to log1).toInterpolator()

    return { t -> Math.pow(base, interpolator(t)) * sign }
}

/**
 * Returns a function that goes from a value `x` in the given interval to a value
 * in the [0, 1] interval.
 *
 * The log of the given interval and value `x` is taken first to produce the interval
 * for the exponent, which is the interval that actually gets deinterpolated.
 */
fun Interval.toLogDeinterpolator(base: Double): (t: Double) -> Double {
    val sign = Math.signum(this.first)
    val otherSign = Math.signum(this.second)

    if (sign != otherSign) {
        throw Exception("The interval should be strictly positive or strictly negative.")
    }

    val log0 = log(this.first * sign, base)
    val log1 = log(this.second * sign, base)

    return { x -> (log(x * sign, base) - log0) / (log1 - log0) }
}

fun logInterpolate(domain: Interval, range: Interval, base: Double): (x: Double) -> Double {
    val logDeinterpolator = domain.toLogDeinterpolator(base)
    val interpolator = range.toInterpolator()

    return { x -> interpolator(logDeinterpolator(x)) }
}

fun logDeinterpolate(range: Interval, domain: Interval, base: Double): (x: Double) -> Double {
    val deinterpolator = range.toDeinterpolator()
    val logInterpolator = domain.toLogInterpolator(base)

    return { x -> logInterpolator(deinterpolator(x)) }
}