package dv

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.test.assertEquals

//@RunWith(JUnitPlatform::class)
//class LinearInterpolatorSpek : Spek({
//    describe("Interval.toInterpolator") {
//        it("should work") {
//            val interval = 100.0 to 200.0
//            val i1 = interval.toInterpolator()
//
//            assertEquals(100.0, i1(0.0))
//            assertEquals(150.0, i1(0.5))
//            assertEquals(200.0, i1(1.0))
//            assertEquals(50.0, i1(-0.5))
//            assertEquals(250.0, i1(1.5))
//        }
//    }
//})

class LinearInterpolatorTest {

    @Test
    fun toInterpolatorTest() {
        val interval: Interval = 100.0 to 200.0
        val interpolator = interval.toInterpolator()

        assertEquals(50.0, interpolator(-0.5))
        assertEquals(100.0, interpolator(0.0))
        assertEquals(150.0, interpolator(0.5))
        assertEquals(200.0, interpolator(1.0))
        assertEquals(250.0, interpolator(1.5))
    }

    @Test
    fun toDeinterpolatorTest() {
        val interval: Interval = 100.0 to 200.0
        val deinterpolator = interval.toDeinterpolator()

        assertEquals(-0.5, deinterpolator(50.0))
        assertEquals(0.0, deinterpolator(100.0))
        assertEquals(0.5, deinterpolator(150.0))
        assertEquals(1.0, deinterpolator(200.0))
        assertEquals(1.5, deinterpolator(250.0))
    }

    @Test
    fun interpolator() {
        val domain: Interval = 5.0 to 10.0
        val range: Interval = 100.0 to 200.0
        val domainToRange = interpolator(domain, range)
        val rangeToDomain = interpolator(range, domain)

        assertEquals(80.0, domainToRange(4.0))
        assertEquals(100.0, domainToRange(5.0))
        assertEquals(150.0, domainToRange(7.5))
        assertEquals(200.0, domainToRange(10.0))
        assertEquals(220.0, domainToRange(11.0))

        assertEquals(4.0, rangeToDomain(80.0))
        assertEquals(5.0, rangeToDomain(100.0))
        assertEquals(7.5, rangeToDomain(150.0))
        assertEquals(10.0, rangeToDomain(200.0))
        assertEquals(11.0, rangeToDomain(220.0))

        val domainValue = 8.0
        val rangeValue = 180.0

        assertEquals(domainValue, rangeToDomain(domainToRange(domainValue)))
        assertEquals(rangeValue, domainToRange(rangeToDomain(rangeValue)))
    }
}