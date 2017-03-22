package dv

fun log(x: Double, base: Double): Double {
    return when (base) {
        Math.E -> Math.log(x)
        10.0 -> Math.log10(x)
        else -> Math.log(x) / Math.log(base)
    }
}

fun Interval.toLogInterpolator(base: Double): (t: Double) -> Double {
    val sign0 = Math.signum(this.first)
    val sign1 = Math.signum(this.second)

    if (sign0 != sign1) {
        throw Exception("The interval should be strictly positive or strictly negative.")
    }

    val log0 = log(this.first, base)
    val log1 = log(this.second, base)

    val interpolator = (log0 to log1).toInterpolator()

    return { t -> Math.pow(base, interpolator(t)) }
}

fun Interval.toLogDeinterpolator(base: Double): (t: Double) -> Double {
    val sign0 = Math.signum(this.first)
    val sign1 = Math.signum(this.second)

    if (sign0 != sign1) {
        throw Exception("The interval should be strictly positive or strictly negative.")
    }

    val log0 = log(this.first, base)
    val log1 = log(this.second, base)

    return { x -> (log(x, base) - log0) / (log1 - log0) }
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