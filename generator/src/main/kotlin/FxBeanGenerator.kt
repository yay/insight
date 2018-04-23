package com.vitalyk.insight.processors

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import javafx.beans.property.SimpleStringProperty
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

annotation class FxBean

@AutoService(Processor::class)
class FxBeanGenerator : AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }

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
                generateClass(className, packageName, it.javaClass)
            }
        return true
    }


    private fun generateClass(className: String, packageName: String, cls: Class<*>) {
        val fileName = className + "FxBean"

        val file = FileSpec.builder(packageName, fileName)
            .addType(TypeSpec.classBuilder(fileName)
                .addProperty(PropertySpec.builder("nameProperty", SimpleStringProperty::class).initializer("SimpleStringProperty()").build())
                .addFunction(FunSpec.builder("getName")
                    .addStatement("return \"Hey\"")
                    .build())
                .build()
            ).build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}