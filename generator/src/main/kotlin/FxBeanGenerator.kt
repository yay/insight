package com.vitalyk.insight.processors

import com.google.auto.service.AutoService
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@Target(AnnotationTarget.CLASS)
annotation class FxBean

@AutoService(Processor::class)
class FxBeanGenerator : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(FxBean::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(FxBean::class.java)
            .forEach {
                val className = it.simpleName.toString()
                val packageName = processingEnv.elementUtils.getPackageOf(it).toString()

                val metaDataClass = Class.forName("kotlin.Metadata").asSubclass(Annotation::class.java)
                val isKotlinClass = it.getAnnotation(metaDataClass) != null

                // You cannot get a Class<?> object during processing because the classes that you want
                // definition of are being compiled right now.

                if (isKotlinClass) {
                    val fileName = className + "FxBean"
                    val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                    val classNames = mutableListOf<String>()

                    val methods = roundEnv.rootElements.find {
                        classNames.add("// " + it.simpleName.toString())
                        it.simpleName.toString() == className // this is never true
                    }?.let {
                        val methods = mutableListOf<ExecutableElement>()
                        it.enclosedElements.forEach {
                            if (it is ExecutableElement) {
                                methods.add(it)
                            }
                        }
                        methods
                    }

                    File(kaptKotlinGeneratedDir, "$fileName.kt").writeText(classNames.joinToString("\n"))

                    if (methods != null && methods.isNotEmpty()) {
                        generateClass(className, packageName, methods)
                    }
                }
            }
        return true
    }


    private fun generateClass(className: String, packageName: String, methods: List<ExecutableElement>) {
        val fileName = className + "FxBean"

        val indent = " ".repeat(4)

        val commentSB = StringBuilder()

        val imports = mutableSetOf<String>() // using a set to avoid duplicate imports
        val headSB = StringBuilder()
        headSB.append("package ").append(packageName).append("\n\n")

        val sb = StringBuilder()
        sb.append("class ").append(className).append("FxBean").append(" {\n")

        methods.forEach {
            commentSB.append("// ").append(it.simpleName.toString()).append("\n")
        }
        commentSB.append("\n\n\n\n")

        methods.filter { it.simpleName.toString().startsWith("get") }.forEach {
            val name = it.simpleName.toString().substring(3).decapitalize()
            val type = it.returnType.toString() // e.g. 'Date'

            val el = processingEnv.typeUtils.asElement(it.returnType)
            val pkg = processingEnv.elementUtils.getPackageOf(el).qualifiedName.toString()
//            pkg.qualifiedName.toString()
//            pkg.simpleName.toString()
//            val pack = it.returnType.`package`?.name  // e.g. 'java.util' (this will be null for primitive types)

            commentSB.append("// name: ").append(name).append("\n")
            commentSB.append("// type: ").append(type).append("\n")
            commentSB.append("// pack: ").append(pkg).append("\n\n")

            sb.append(indent)
            sb.append("val ").append(name).append("Property = ")
            var property = javaTypeToPropertyMap[type]
            if (property != null) { // primitive type
                sb.append(property)
            } else {
                property = "SimpleObjectProperty<$type>"
                sb.append(property)
//                sb.append("SimpleObjectProperty<").append(type).append(">")
                val javaImport = "$pkg.$type"
                val kotlinImport = javaToKotlinImportMap[javaImport]
                if (kotlinImport == null) {
                    imports.add(javaImport)
                } else if (kotlinImport.isNotEmpty()) {
                    imports.add(kotlinImport)
                }
                // javafx.beans.property.SimpleObjectProperty
//                headSB.append("import ").append(pack).append(".").append(type).append("\n")
            }
            sb.append("()\n")

            sb.append(indent).append("var ").append(name).append(": ").append(javaToKotlinTypeMap[type] ?: type).append("\n")
            sb.append(indent.repeat(2)).append("get() = ").append(property).append(".get()\n")
            sb.append(indent.repeat(2)).append("set(value) = ").append(property).append(".set(value)\n\n")
        }
        sb.append("}")

        imports.forEach {
            headSB.append("import ").append(it).append("\n")
        }
        headSB.append("\n\n").append(sb.toString()).append("\n\n").append(commentSB.toString())

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        File(kaptKotlinGeneratedDir, "$fileName.kt").writeText(headSB.toString())
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        val javaTypeToPropertyMap = mapOf(
            "boolean" to "SimpleBooleanProperty",
            "int" to "SimpleIntegerProperty",
            "long" to "SimpleLongProperty",
            "float" to "SimpleFloatProperty",
            "double" to "SimpleDoubleProperty",
            "String" to "SimpleStringProperty"
        )

        val javaToKotlinTypeMap = mapOf(
            "boolean" to "Boolean",
            "int" to "Int",
            "long" to "Long",
            "float" to "Float",
            "double" to "Double",
            "String" to "String"
        )

        // Value not found (null) = use Java import
        // Found value is empty string = skip import
        val javaToKotlinImportMap = mapOf(
            "java.util.Set" to "",
            "java.util.List" to ""
        )

        val kotlinTypeToPropertyMap = mapOf(
            "kotlin.Boolean" to "SimpleBooleanProperty",
            "kotlin.Int" to "SimpleIntegerProperty",
            "kotlin.Long" to "SimpleLongProperty",
            "kotlin.Float" to "SimpleFloatProperty",
            "kotlin.Double" to "SimpleDoubleProperty",
            "kotlin.String" to "SimpleStringProperty"
        )
    }
}