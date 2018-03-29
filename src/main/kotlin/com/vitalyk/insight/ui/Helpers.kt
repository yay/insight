package com.vitalyk.insight.ui

import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import tornadofx.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

// Takes a data class definition and produces a bean definition.
fun getFxBeanDefinition(klass: KClass<*>): String {
    val indent = " ".repeat(4)
    val kclassProps = klass.declaredMemberProperties
    val beanConstructor = kclassProps.map { prop ->
        "${indent}${prop.name}: ${prop.returnType.toString().split(".").last()}"
    }
    val beanProps = kclassProps.map { prop ->
        "${indent}var ${prop.name}: ${prop.returnType.toString().split(".").last()} by property(${prop.name})\n" +
        "${indent}fun ${prop.name}Property() = getProperty(${klass.simpleName}Bean::${prop.name})\n"
    }
    return "open class ${klass.simpleName}Bean(\n" +
        beanConstructor.joinToString(",\n") + "\n) {\n" +
        beanProps.joinToString("\n") + "}"
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
fun toBeanMaker(klass: KClass<*>): String {
    val indent = " ".repeat(4)

    return "fun ${klass.simpleName}.toBean() =\n" +
        "$indent${klass.simpleName}Bean().let {\n" +
        klass.declaredMemberProperties.map { prop ->
            "$indent${indent}it.${prop.name} = this.${prop.name}"
        }.joinToString("\n") +
        "\n$indent${indent}it\n" +
        "$indent}"
}

// Takes a data class and generates a method that transforms it to a corresponding bean.
fun getOldBeanMaker(klass: KClass<*>): String {
    val indent = " ".repeat(4)

    return "fun ${klass.simpleName}.to${klass.simpleName}Bean() =\n" +
        "$indent${klass.simpleName}Bean(\n" +
        klass.declaredMemberProperties.map { prop ->
            "$indent$indent${prop.name} = this.${prop.name}"
        }.joinToString(",\n") +
        "\n$indent)"
}