package com.vitalyk.insight.dv

class LinearScale {
    var domain: Interval = 0.0 to 1.0
        set(value) {
            field = value
            rescale()
        }

    var range: Interval = 0.0 to 1.0
        set(value) {
            field = value
            rescale()
        }

    private var domainToRange = linearInterpolator(domain, range)
    private var rangeToDomain = linearInterpolator(range, domain)

    fun rescale() {
        domainToRange = linearInterpolator(domain, range)
        rangeToDomain = linearInterpolator(range, domain)
    }

    fun toRange(value: Double) = domainToRange(value)
    fun toDomain(value: Double) = rangeToDomain(value)
}