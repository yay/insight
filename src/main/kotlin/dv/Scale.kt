package dv

typealias Interval = Pair<Double, Double>

fun Interval.toInterpolator(): (t: Double) -> Double = { t -> this.first * (1 - t) + this.second * t }
fun Interval.toDeinterpolator(): (x: Double) -> Double = { x -> (x - this.first) / (this.second - this.first) }

infix fun Interval.to(that: Interval): (x: Double) -> Double {
    val deinterpolator = this.toDeinterpolator()
    val interpolator = that.toInterpolator()

    return { x -> interpolator(deinterpolator(x)) }
}

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