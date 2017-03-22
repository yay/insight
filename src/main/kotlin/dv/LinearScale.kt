package dv

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

    private var domainToRange = domain to range
    private var rangeToDomain = range to domain

    fun rescale() {
        domainToRange = domain to range
        rangeToDomain = range to domain
    }

    fun toRange(value: Double) = domainToRange(value)
    fun toDomain(value: Double) = rangeToDomain(value)
}