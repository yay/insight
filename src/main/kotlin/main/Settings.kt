package main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A singleton for loading and saving settings objects.
 */
object Settings {

    private val mapper by lazy { jacksonObjectMapper() }

    /**
     * Creates a filename for the given settings object based on its class name.
     */
    private fun getFileName(obj: Any): String = "${obj::class.java.name.split(".").last()}.json"

    fun load(obj: Any, filename: String = getFileName(obj)) {
        mapper.readValue(File(filename), obj::class.java)
    }

    fun save(obj: Any, filename: String = getFileName(obj)) {
        mapper.writerWithDefaultPrettyPrinter().writeValue(File(filename), obj)
    }

    private var saveOnShutdownMap = mutableMapOf<String, Boolean>()

    /**
     * Adds a hook to save the given settings object on shutdown using the specified filename.
     */
    fun saveOnShutdown(obj: Any, filename: String = getFileName(obj)) {
        if (saveOnShutdownMap[filename] != true) {
            Runtime.getRuntime().addShutdownHook(Thread {
                mapper.writerWithDefaultPrettyPrinter().writeValue(File(filename), obj)
            })
            saveOnShutdownMap[filename] = true
        } else {
            getAppLogger().warn("${getFileName(obj)} is already set to save on shutdown.")
        }
    }
}