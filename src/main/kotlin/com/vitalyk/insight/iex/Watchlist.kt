package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.IexApi.Tops
import com.vitalyk.insight.main.getAppLogger
import io.socket.client.IO
import io.socket.client.Socket
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener

class Watchlist {

    companion object {
        private val watchlists = mutableSetOf<Watchlist>()

        private fun generateName(watchlist: Watchlist): String {
            // There is a tiny chance that the user will set the name of one of the existing
            // watchlists to the name we generate here for a new watchlist.
            // So, if only in theory, the constructor might throw.
            val name = "Watchlist ${watchlist.hashCode()}"
            register(watchlist, name)
            return name
        }

        private fun register(watchlist: Watchlist, name: String) {
            watchlists.find { it.name == name }?.let {
                throw IllegalArgumentException("A watchlist with this name already exists.")
            }
            watchlists.add(watchlist)
        }

        fun deregister(watchlist: Watchlist) {
            watchlists.remove(watchlist)
        }

        fun clearAll() {
            watchlists.forEach {
                it.clearListeners()
                it.clearSymbols()
            }
            watchlists.clear()
        }
    }

    private val map = FXCollections.observableHashMap<String, Tops>()
    private val listeners = mutableSetOf<MapChangeListener<String, Tops>>()
    val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    var name: String = generateName(this)
        @Throws(IllegalArgumentException::class)
        set(value) { // Note: the setter is not triggered by the default (generated) value
            if (name == value) return
            register(this, value)
            field = value
        }

    // This read-only property won't show the added symbols immediately,
    // only after they've been successfully fetched.
    val symbols: List<String>
        get() = map.keys.toList()

    val tops: List<Tops>
        get() = map.values.toList()

    constructor(symbols: List<String>) {
        addSymbols(symbols)
    }

    constructor(vararg symbols: String) {
        addSymbols(symbols.toList())
    }

    fun addListener(listener: (MapChangeListener.Change<out String, out Tops>) -> Unit): MapChangeListener<String, Tops> {
        return MapChangeListener<String, Tops> { change -> listener(change) }.apply {
            map.addListener(this)
            listeners.add(this)
        }
    }

    fun removeListener(listener: MapChangeListener<String, Tops>) {
        listeners.remove(listener)
        map.removeListener(listener)
    }

    fun clearListeners() = listeners.forEach {
        map.removeListener(it)
    }

    init {
        socket
            .on(Socket.EVENT_MESSAGE) { params ->
                val tops = IexApi.parseTops(params.first() as String)
                // This function can be called for a given symbol even if no change occurred:
                // no bid/ask size, price or even volume changes. The contents of data class
                // instances will be identical. That's why we need an observable map,
                // it's listener will only be called when an actual change happens.
                map[tops.symbol] = tops
            }
            .on(Socket.EVENT_DISCONNECT) {
                getAppLogger().debug("Watchlist disconnected: ${map.keys}")
            }
    }

    fun isConnected() = socket.connected()

    operator fun contains(key: String) = key in map
    operator fun get(key: String) = map[key]

    fun addSymbols(symbols: List<String>) {
        val new = symbols.filter { it !in map }
        if (new.isEmpty()) return

        socket.connect()
        socket.emit("subscribe", new.joinToString(","))
    }

    fun addSymbols(vararg symbols: String) {
        addSymbols(symbols.toList())
    }

    fun removeSymbols(symbols: List<String>) {
        val old = symbols.filter { it in map }
        old.forEach { map.remove(it) }

        if (map.keys.isEmpty()) {
            // If no threads are running, this will result in program exit.
            socket.disconnect()
        }
        socket.emit("unsubscribe", old.joinToString(","))
    }

    fun removeSymbols(vararg symbols: String) {
        removeSymbols(symbols.toList())
    }

    fun clearSymbols() {
        map.clear()
        socket.disconnect()
        socket.off() // Remove all listeners.
    }
}
