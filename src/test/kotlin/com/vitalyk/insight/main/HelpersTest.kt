package com.vitalyk.insight.main

import com.vitalyk.insight.helpers.dropSingle
import com.vitalyk.insight.helpers.dropSingleSquashSequence
import com.vitalyk.insight.helpers.takeOneOffSequence
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class HelpersTest {

    @Test
    fun dropSingleSquashSequenceTest() {
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
    fun takeOneOffSequenceTest() {
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
    fun dropSingleTest() {
        val actual1 = dropSingle("o ooo o oo o", 'o')
        val expected1 = " ooo  oo "
        assertEquals(expected1, actual1)

        val actual2 = dropSingle("o", 'o')
        val expected2 = ""
        assertEquals(expected2, actual2)

        val actual3 = dropSingle("oo", 'o')
        val expected3 = "oo"
        assertEquals(expected3, actual3)

        val actual4 = dropSingle("o ooo o oo o", 'o', "__")
        val expected4 = "__ ooo __ oo __"
        assertEquals(expected4, actual4)
    }
}