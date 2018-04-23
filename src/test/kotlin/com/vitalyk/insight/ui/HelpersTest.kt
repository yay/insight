package com.vitalyk.insight.ui

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class HelpersTest {

    data class TestClass1(
        val date: Date,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long,
        val unadjustedVolume: Long,
        val change: Double,
        val changePercent: Double,
        val vwap: Double,
        val label: String,
        val changeOverTime: Double
    )

    val testBean1 = """
    open class TestClassBean(
        change: Double,
        changeOverTime: Double,
        changePercent: Double,
        close: Double,
        date: Date,
        high: Double,
        label: String,
        low: Double,
        open: Double,
        unadjustedVolume: Long,
        volume: Long,
        vwap: Double
    ) {
        var change: Double by property(change)
        fun changeProperty() = getProperty(TestClassBean::change)

        var changeOverTime: Double by property(changeOverTime)
        fun changeOverTimeProperty() = getProperty(TestClassBean::changeOverTime)

        var changePercent: Double by property(changePercent)
        fun changePercentProperty() = getProperty(TestClassBean::changePercent)

        var close: Double by property(close)
        fun closeProperty() = getProperty(TestClassBean::close)

        var date: Date by property(date)
        fun dateProperty() = getProperty(TestClassBean::date)

        var high: Double by property(high)
        fun highProperty() = getProperty(TestClassBean::high)

        var label: String by property(label)
        fun labelProperty() = getProperty(TestClassBean::label)

        var low: Double by property(low)
        fun lowProperty() = getProperty(TestClassBean::low)

        var open: Double by property(open)
        fun openProperty() = getProperty(TestClassBean::open)

        var unadjustedVolume: Long by property(unadjustedVolume)
        fun unadjustedVolumeProperty() = getProperty(TestClassBean::unadjustedVolume)

        var volume: Long by property(volume)
        fun volumeProperty() = getProperty(TestClassBean::volume)

        var vwap: Double by property(vwap)
        fun vwapProperty() = getProperty(TestClassBean::vwap)
    }
    """.trimIndent()

    val instanceToBean =
    """
    fun TestClass.toBean() =
        TestClassBean().let {
            it.change = this.change
            it.changeOverTime = this.changeOverTime
            it.changePercent = this.changePercent
            it.close = this.close
            it.date = this.date
            it.high = this.high
            it.label = this.label
            it.low = this.low
            it.open = this.open
            it.unadjustedVolume = this.unadjustedVolume
            it.volume = this.volume
            it.vwap = this.vwap
            it
        }
    """.trimIndent()

    data class TestClass2(
        val bool: Boolean,
        val str: String,
        val double: Double,
        val float: Float,
        val int: Int,
        val long: Long,
        val date: Date
    )

    val testBean2 = """
        package com.vitalyk.insight.ui

        import tornadofx.*

        class TestClass2FxBean {
            val boolProperty = SimpleBooleanProperty()
            var bool by boolProperty
            val dateProperty = SimpleObjectProperty<java.util.Date>()
            var date by dateProperty
            val doubleProperty = SimpleDoubleProperty()
            var double by doubleProperty
            val floatProperty = SimpleFloatProperty()
            var float by floatProperty
            val intProperty = SimpleIntegerProperty()
            var int by intProperty
            val longProperty = SimpleLongProperty()
            var long by longProperty
            val strProperty = SimpleStringProperty()
            var str by strProperty
        }
    """.trimIndent()

    @Test
    fun getOldFxBeanDefinition() {
        assertEquals(testBean1, getOldFxBeanDefinition(TestClass1::class))
    }

    @Test
    fun getBeanMaker() {
        assertEquals(instanceToBean, toBeanMaker(TestClass1::class))
    }

    @Test
    fun getFxBeanDefinition() {
        assertEquals(testBean2, getFxBeanDefinition(TestClass2::class))
    }
}