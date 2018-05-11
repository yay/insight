package com.vitalyk.insight.helpers

import com.vitalyk.insight.Insight
import com.vitalyk.insight.iex.Iex
import javafx.scene.media.AudioClip
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

/**
 * E.g. getResourceAudioClip("/sounds/alerts/chime.wav").play()
 */
fun getResourceAudioClip(path: String) = AudioClip(Insight::class.java.getResource(path).toURI().toString())

private val typeToFxPropertyMap = mapOf(
    "kotlin.Boolean" to "SimpleBooleanProperty",
    "kotlin.Short" to "SimpleIntegerProperty",
    "kotlin.Int" to "SimpleIntegerProperty",
    "kotlin.Long" to "SimpleLongProperty",
    "kotlin.Float" to "SimpleFloatProperty",
    "kotlin.Double" to "SimpleDoubleProperty",
    "kotlin.String" to "SimpleStringProperty"
)

data class TestClass(
    val bool: Boolean,
    val short: Short,
    val int: Int,
    val long: Long,
    val float: Float?,
    val double: Double,
    val str: String,
    val date: Date,
    val list: List<Date>,
    val set: Set<Date>
)

// TODO: check with nullable types, lists, sets and other types not in the typeToPropertyMap.
fun getFxBeanDefinition(klass: KClass<*>): String {
    val indent = " ".repeat(4)

    val imports = mutableSetOf<String>() // using a set to avoid duplicate imports
    val headSB = StringBuilder()
    headSB.append("package ").append(klass.java.`package`.name).append("\n\n")
    headSB.append("import tornadofx.*").append("\n")

    val sb = StringBuilder()
    sb.append("class ").append(klass.simpleName).append("FxBean").append(" {\n")

    klass.memberProperties.forEach {
        sb.append(indent)
        sb.append("val ").append(it.name).append("Property = ")
        val type = it.returnType.toString()
        // `type` can be nullable or not, e.g.: `kotlin.Double` or `kotlin.Double?`
        val property = typeToFxPropertyMap[type.replace("?", "")]
        if (property != null) { // primitive type
            sb.append(property)
        } else {
            imports.add(type)
            sb.append("SimpleObjectProperty<").append(type).append(">")
        }
        sb.append("()\n")

        sb.append(indent)
        sb.append("var ").append(it.name).append(" by ").append(it.name).append("Property\n")
    }
    sb.append("}")

    imports.forEach {
        headSB.append("import ").append(it).append("\n")
    }

    headSB.append("\n\n").append(sb.toString())

    return headSB.toString()
}

fun main(args: Array<String>) {
//    println(getFxBeanDefinition(Iex.AssetStats::class))
    println(getToFxBeanAltDefinition(Iex.AssetStats::class))
}

// Takes a data class and creates a table view creation code
// that uses the bean generated by the `getFxBeanDefinition` function.
fun getFxBeanTableView(klass: KClass<*>): String {
    val beanName = "${klass.simpleName}Bean"
    val indent = " ".repeat(4)

    return "tableview(listOf<$beanName>().observable()) {\n" +
        klass.declaredMemberProperties.map { prop ->
            "${indent}column(\"${prop.name.capitalize()}\", $beanName::${prop.name}Property)"
        }.joinToString("\n") +
        "\n}"
}

// Takes a data class and generates a method that transforms it to a corresponding bean.
fun getToFxBeanDefinition(klass: KClass<*>): String {
    val indent = " ".repeat(4)

    return "fun ${klass.simpleName}.toFxBean() =\n" +
        "$indent${klass.simpleName}FxBean().let {\n" +
        klass.declaredMemberProperties.map { prop ->
            "$indent${indent}it.${prop.name} = ${prop.name}"
        }.joinToString("\n") +
        "\n$indent${indent}it\n" +
        "$indent}"
}

fun getToFxBeanAltDefinition(klass: KClass<*>): String {
    val indent = " ".repeat(4)

    return "fun ${klass.simpleName}.toFxBean(bean: ${klass.simpleName}FxBean) {\n" +
        klass.declaredMemberProperties.map { prop ->
            "${indent}bean.${prop.name} = ${prop.name}"
        }.joinToString("\n") + "\n}"
}