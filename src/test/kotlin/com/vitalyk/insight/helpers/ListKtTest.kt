package com.vitalyk.insight.helpers

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ListKtTest {

    @Test
    fun aggregate() {
        val list = listOf(2, 8, 4, 6, 7, 3)
        val list2 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

        assertEquals(listOf(5.0, 5.0, 5.0), list.aggregate(3, { it.average() }))
        assertEquals(listOf(5.333333333333333, 4.666666666666667), list.aggregate(2, { it.average() }))
        assertEquals(listOf(5.0), list.aggregate(1, { it.average() }))
        assertEquals(listOf(3.0, 7.0, 6.0, 4.0, 8.0, 2.0), list.aggregate(6, { it.average() }))
        assertEquals(listOf(3.0, 7.0, 6.0, 4.0, 8.0, 2.0), list.aggregate(4, { it.average() }))
        assertEquals(listOf(14.0, 11.0, 8.0, 5.0, 2.0), list2.aggregate(4, { it.average() }))
    }
}