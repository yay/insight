package com.vitalyk.insight.helpers

/**
 * Replaces sequences of `char` with a single `char`, while removing standalone `char`s.
 */
fun dropSingleSquashSequence(text: String, char: Char): String {
    val sb = StringBuilder()
    if (text.isNotEmpty()) {
        var prev: Char? = null
        var lastAppended: Char? = null
        text.forEach {
            val skip = it == char && (prev != char || lastAppended == char)
            if (!skip) {
                sb.append(it)
                lastAppended = it
            }
            prev = it
        }
    }
    return sb.toString()
}

/**
 * Makes sequences of `char` one `char` shorter, while removing standalone `char`s.
 */
fun takeOneOffSequence(text: String, char: Char): String {
    val sb = StringBuilder()
    if (text.isNotEmpty()) {
        var skippedPrev = false
        text.forEach {
            val skip = it == char && !skippedPrev
            if (!skip) {
                sb.append(it)
                if (it != char) skippedPrev = false
            } else skippedPrev = true
        }
    }
    return sb.toString()
}

/**
 * Removes/replaces standalone `char`s, while keeping sequences of `char`.
 */
fun dropSingle(text: String, char: Char, replacement: String = ""): String {
    val sb = StringBuilder()
    if (text.isNotEmpty()) {
        val isReplace = replacement.isNotEmpty()
        val last = text.lastIndex
        text.forEachIndexed { i, c ->
            val pi = i - 1
            val ni = i + 1
            val prev = if (pi >= 0) text[pi] else null
            val next = if (ni <= last) text[ni] else null
            val skip = c == char && prev != char && next != char
            if (!skip)
                sb.append(c)
            else if (isReplace) {
                sb.append(replacement)
            }
        }
    }
    return sb.toString()
}