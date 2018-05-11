package com.vitalyk.insight.trigger

data class TextTrigger(
    val value: String,
    val type: Type
) {
    enum class Type {
        KEYWORDS,
        REGEX,
        SCRIPT
    }

    fun check(text: String): Boolean {
        return when (type) {
            Type.KEYWORDS -> {
                val words = getWords(text)
                val keywords = value.toLowerCase().split(",").map { it.trim() }
                keywords.all { it in words }
            }
            Type.REGEX -> {
                val regex = value.toRegex()
                regex.containsMatchIn(text)
            }
            Type.SCRIPT -> {
                false
            }
        }
    }
}

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

    println(text1)
    println(text2)
    println(text3)
    println(text4)
    println(text5)
    println(text6)
    println(text7)
}