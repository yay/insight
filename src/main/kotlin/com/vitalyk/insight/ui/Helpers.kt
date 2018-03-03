package com.vitalyk.insight.ui

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

fun dataClassToFxBean(klass: KClass<*>): String {
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