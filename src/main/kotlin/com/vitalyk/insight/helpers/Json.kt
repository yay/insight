package com.vitalyk.insight.helpers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
}
val objectWriter: ObjectWriter = objectMapper.writerWithDefaultPrettyPrinter()

fun String.toJsonNode(): JsonNode {
    return try {
        objectMapper.readTree(this)
    } catch (e: JsonProcessingException) {
        objectMapper.readTree("{}")
    }
}

fun String.toPrettyJson(): String = objectWriter.writeValueAsString(this.toJsonNode())

fun Any.toPrettyJson(): String = objectWriter.writeValueAsString(this)