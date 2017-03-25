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

    private var domainToRange = interpolator(domain, range)
    private var rangeToDomain = interpolator(range, domain)

    fun rescale() {
        domainToRange = interpolator(domain, range)
        rangeToDomain = interpolator(range, domain)
    }

    fun toRange(value: Double) = domainToRange(value)
    fun toDomain(value: Double) = rangeToDomain(value)
}