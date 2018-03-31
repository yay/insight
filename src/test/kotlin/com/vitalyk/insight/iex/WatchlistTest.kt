package com.vitalyk.insight.iex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class WatchlistTest {

    @Test
    fun construction() {
        val wl1 = Watchlist()
        wl1.name = "Main" // should not throw: "Main" is a unique name
        wl1.name = "Main" // should not throw: not changing anything

        val wl2 = Watchlist()
        wl2.name = "DJI"

        assertThrows<IllegalArgumentException>("Watchlist names should be unique") {
            wl2.name = wl1.name
        }

        assertEquals("DJI", wl2.name)

        var wl3: Watchlist? = Watchlist()
        wl3?.name = "Positions"

        wl3 = null  // remove the only reference to wl3
        System.gc() // make sure it also disappears from the static weak set in the Watchlist

        wl3 = Watchlist()
        wl3.name = "Positions" // should not throw

    }
}