package com.vitalyk.insight.main

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class HelpersTest {

    @Test
    fun dropSingleSquashSequence() {
        val actual1 = dropSingleSquashSequence("o ooo o oo o", 'o')
        val expected1 = " o  o "
        assertEquals(expected1, actual1)

        val actual2 = dropSingleSquashSequence("o", 'o')
        val expected2 = ""
        assertEquals(expected2, actual2)

        val actual3 = dropSingleSquashSequence("oooo", 'o')
        val expected3 = "o"
        assertEquals(expected3, actual3)
    }

    @Test
    fun takeOneOffSequence() {
        val actual1 = takeOneOffSequence("o ooo o oo o", 'o')
        val expected1 = " oo  o "
        assertEquals(expected1, actual1)

        val actual2 = takeOneOffSequence("o", 'o')
        val expected2 = ""
        assertEquals(expected2, actual2)

        val actual3 = takeOneOffSequence("oooo", 'o')
        val expected3 = "ooo"
        assertEquals(expected3, actual3)
    }

    @Test
    fun dropSingle() {
        val actual1 = dropSingle("o ooo o oo o", 'o')
        val expected1 = " ooo  oo "
        assertEquals(expected1, actual1)

        val actual2 = dropSingle("o", 'o')
        val expected2 = ""
        assertEquals(expected2, actual2)

        val actual3 = dropSingle("oo", 'o')
        val expected3 = "oo"
        assertEquals(expected3, actual3)
    }
}