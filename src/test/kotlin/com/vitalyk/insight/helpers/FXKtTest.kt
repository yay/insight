package com.vitalyk.insight.helpers

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class FXKtTest {

    data class TestCls(
        val bool: Boolean,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float?,
        val double: Double,
        val str: String,
        val date: Date
    )

    val testClsBean = """
        package com.vitalyk.insight.helpers

        import tornadofx.*
        import java.util.Date


        class TestClsFxBean {
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
            val shortProperty = SimpleIntegerProperty()
            var short by shortProperty
            val strProperty = SimpleStringProperty()
            var str by strProperty
        }
    """.trimIndent()

    @Test
    fun getFxBeanDefinition() {
        assertEquals(testClsBean, getFxBeanDefinition(TestCls::class))
    }
}