package com.vitalyk.insight.dv.scale

fun polymap(domain: List<Double>, range: List<Double>,
            deinterpolate: (x: Double) -> Double, reinterpolate: (t: Double) -> Double) {
    val j = Math.min(domain.size, range.size) - 1
    val d = DoubleArray(j)
    val r = DoubleArray(j)
    val i = -1

    var domain = domain
    var range = range
    if (domain[j] < domain[0]) {
        domain = domain.reversed()
        range = range.reversed()
    }

    val (_domain, _range) = if (domain[j] < domain[0])
        Pair(domain.reversed(), range.reversed())
    else
        Pair(domain, range)
}