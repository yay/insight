package com.vitalyk.insight.iex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class WatchlistTest {

    @Test
    fun construction() {
        val wl1 = Watchlist("Main")
        wl1.name = "Main" // should not throw: name already taken, but hasn't changed

        val wl2 = Watchlist("DJI") // should not throw: name not taken

        assertThrows<IllegalArgumentException>("Watchlist names should be unique") {
            wl2.name = wl1.name
        }

        assertEquals("DJI", wl2.name) // name hasn't changed after exception

        val wl3 = Watchlist("Positions")
        Watchlist.deregister(wl3)

        Watchlist("Positions") // should not throw: name was deregistered
    }
}