package com.vitalyk.insight.dv

class LogScale {
    var base: Double = 10.0
        set(value) {
            field = value
            rescale()
        }

    var domain: Interval = 1.0 to 10.0
        set(value) {
            field = value
            rescale()
        }

    var range: Interval = 0.0 to 1.0
        set(value) {
            field = value
            rescale()
        }

    // TODO: logInterpolator and logDeinterpolator are probably called too many times
    // during initialization
    // https://kotlinlang.org/docs/reference/classes.html
    private var domainToRange = logInterpolator(domain, range, base)
    private var rangeToDomain = logDeinterpolator(range, domain, base)

    fun rescale() {
        domainToRange = logInterpolator(domain, range, base)
        rangeToDomain = logDeinterpolator(range, domain, base)
    }

    fun toRange(value: Double) = domainToRange(value)
    fun toDomain(value: Double) = rangeToDomain(value)
}