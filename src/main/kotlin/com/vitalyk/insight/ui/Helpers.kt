package com.vitalyk.insight.ui

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

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

fun getFxBeanTableView(klass: KClass<*>): String {
    val beanName = "${klass.simpleName}Bean"
    val indent = " ".repeat(4)

    return "tableview(listOf<$beanName>().observable()) {\n" +
        klass.declaredMemberProperties.map { prop ->
            "${indent}column(\"${prop.name.capitalize()}\", $beanName::${prop.name}Property)"
        }.joinToString("\n") +
        "\n}"
}

fun getBeanMaker(klass: KClass<*>): String {
    val indent = " ".repeat(4)

    return "fun ${klass.simpleName}.to${klass.simpleName}Bean() =\n" +
        "$indent${klass.simpleName}Bean(\n" +
        klass.declaredMemberProperties.map { prop ->
            "$indent$indent${prop.name} = this.${prop.name}"
        }.joinToString(",\n") +
        "\n$indent)"
}