package com.vitalyk.insight.main

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

/**
 * A singleton for loading and saving settings objects.
 */
object Settings {

    private var canSetParentDir = true

    var parentDir = "" // can only be set once
        set(value) {
            if (canSetParentDir)
                canSetParentDir = false
                field = value
        }
        get() {
            canSetParentDir = false
            return field
        }

    private val mapper by lazy { jacksonObjectMapper() }

    /**
     * Creates a filename for the given settings object based on its class name.
     */
    private fun getFileName(obj: Any) = parentDir + "${obj::class.java.simpleName}.json"

    /**
     * Populates the given settings object with values read from JSON file.
     */
    fun <T> load(obj: T, filename: String = getFileName(obj as Any), block: T.() -> Unit = {}): Boolean {
        val file = File(filename)
        if (!file.isFile) return false
        try {
            mapper.readValue(file, (obj as Any)::class.java)
        } catch (e: MismatchedInputException) {
            e.printStackTrace()
            return false
        }
        block(obj)
        return true
    }

    fun save(obj: Any, filename: String = getFileName(obj)) {
        mapper.writerWithDefaultPrettyPrinter().writeValue(File(filename), obj)
    }

    private var saveOnShutdownMap = mutableMapOf<String, Boolean>()

    /**
     * Adds a hook to save the given settings object on shutdown using the specified filename.
     */
    fun <T> saveOnShutdown(obj: T, filename: String = getFileName(obj as Any),
                           block: T.() -> Unit = {}) {
        if (saveOnShutdownMap[filename] != true) {
            Runtime.getRuntime().addShutdownHook(Thread {
                block(obj)
                mapper.writerWithDefaultPrettyPrinter().writeValue(File(filename), obj)
            })
            saveOnShutdownMap[filename] = true
        } else {
            appLogger.warn("${getFileName(obj as Any)} is already set to save on shutdown.")
        }
    }
}