package com.vitalyk.insight.trigger

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.util.regex.PatternSyntaxException

// https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization
// https://github.com/FasterXML/jackson-databind/issues/1641
// https://github.com/FasterXML/jackson-databind/issues/1350

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "@class")
interface TextTrigger {
    // Not using `isRecurring` here because it is serialized as `recurring`
    // and then fails to  deserialize because the Kotlin object
    // has no `recurring` property.
    val recurring: Boolean
    fun check(text: String): Boolean
}

data class AllKeywordsTrigger (
    val keywords: Set<String>,
    override val recurring: Boolean
) : TextTrigger {

    override fun check(text: String): Boolean {
        val words = getWords(text)
        return keywords.all { it in words }
    }

    companion object {
        fun of(text: String, recurring: Boolean = false): AllKeywordsTrigger? {
            val keywords = getWords(text)
            return if (keywords.isNotEmpty())
                AllKeywordsTrigger(keywords, recurring)
            else
                null
        }
    }
}

data class AnyKeywordTrigger (
    val keywords: Set<String>,
    override val recurring: Boolean
) : TextTrigger {

    override fun check(text: String): Boolean {
        val words = getWords(text)
        return keywords.any { it in words }
    }

    companion object {
        fun of(text: String, recurring: Boolean = false): AnyKeywordTrigger? {
            val keywords = getWords(text)
            return if (keywords.isNotEmpty())
                AnyKeywordTrigger(keywords, recurring)
            else
                null
        }
    }
}

private class RegExSerializer : StdSerializer<Regex>(Regex::class.java) {
    override fun serialize(value: Regex, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.pattern)
    }
}

private class RegExDeserializer : StdDeserializer<Regex>(Regex::class.java) {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Regex {
        return parser.readValueAs(String::class.java).toRegex()
    }
}

data class RegExTrigger (
    @JsonSerialize(using = RegExSerializer::class)
    @JsonDeserialize(using = RegExDeserializer::class)
    val regex: Regex,
    override val recurring: Boolean
) : TextTrigger {

    override fun check(text: String): Boolean {
        return regex.containsMatchIn(text)
    }

    companion object {
        fun of(text: String, recurring: Boolean = false): RegExTrigger? {
            return try {
                RegExTrigger(text.toRegex(), recurring)
            } catch (e: PatternSyntaxException) {
                null
            }
        }
    }
}

// TODO: how to test private functions?
private fun getWords(text: String) = text
    .toLowerCase()
    .split(" ", ",", ":", "'", " - ", "?", "\n")
    .filter { it.length > 1 }
    .toSet()

fun main(args: Array<String>) {
    val text1 = getWords("Italy's new government\nto be 'reasonable and rational'\nwith budget: source")
    val text2 = getWords("Britain's Prince Philip, 96, drives to horse show, chats with Queen")
    val text3 = getWords("Factbox - Tesla faces scrutiny after Florida car accident")
    val text4 = getWords("Russia, after Netanyahu visit, backs off Syria S-300 missile supplies")
    val text5 = getWords("Plastics mines? Europe struggles as pollution piles up")
    val text6 = getWords("U.S. safety agency reviewing fatal Tesla crash in Florida")
    val text7 = getWords("Russia's, Russia: Russia - Russia?")
    val text8 = getWords("  UK    UK  UK ")

    println(text1)
    println(text2)
    println(text3)
    println(text4)
    println(text5)
    println(text6)
    println(text7)
    println(text8)
}