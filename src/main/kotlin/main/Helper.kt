package main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException
import java.text.DecimalFormat

fun Long.toReadableNumber(): String {
    // E.g. 10_550_000_000.main.toReadableNumber()  // 10.6B
    if (this <= 0) return "0"
    val units = arrayOf("", "K", "M", "B", "T")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1000.0)).toInt()
    return DecimalFormat("#,##0.#").format(this / Math.pow(1000.0, digitGroups.toDouble())) + units[digitGroups]
}

fun String.writeToFile(filePath: String): Boolean {
    val file = File(filePath)
    var error: String? = null

    try {
        file.parentFile.mkdirs()
        file.writeText(this)
    } catch (e: SecurityException) { // mkdirs
        error = "Creating parent directories for ${file.canonicalPath} failed.\n${e.message}"
    } catch (e: IOException) { // writeText
        error = "Writing to ${file.canonicalPath} failed.\n${e.message}"
    }

    if (error != null) {
        throw Error(error)
    }

    return error == null
}

fun String.toJsonNode(): JsonNode {
    val mapper = ObjectMapper()

    try {
        return mapper.readTree(this)
    } catch (e: JsonProcessingException) {
        return mapper.readTree("{}")
    }
}

fun String.toPrettyJson(): String =
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.toJsonNode())