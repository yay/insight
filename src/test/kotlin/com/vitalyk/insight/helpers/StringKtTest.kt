package com.vitalyk.insight.helpers

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StringKtTest {

    val test1 = "A good  test  is    a good  test"

    @Test
    fun dropSingleSquashSequenceTest() {
        assertEquals("Agood test is agood test", dropSingleSquashSequence(test1, ' '))
    }

    @Test
    fun squashSequenceTest() {
        assertEquals("A good test is a good test", squashSequence(test1, ' '))
    }

    @Test
    fun takeOneOffSequenceTest() {
        assertEquals("Agood test is   agood test", takeOneOffSequence(test1, ' '))
    }

    @Test
    fun dropSingleTest() {
        assertEquals("Agood  test  is    agood  test", dropSingle(test1, ' '))
    }
}