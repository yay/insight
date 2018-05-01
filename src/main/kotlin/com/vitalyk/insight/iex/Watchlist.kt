package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.Iex.Tops
import com.vitalyk.insight.main.appLogger
import io.socket.client.IO
import io.socket.client.Socket

typealias ChangeListener = (old: Tops?, new: Tops?) -> Unit

class Watchlist(name: String, symbols: List<String> = emptyList()) {

    data class Settings(
        val name: String,
        val symbols: List<String>
    )

    companion object {
        private val watchlists = mutableMapOf<String, Watchlist>()

//        private fun generateName(watchlist: Watchlist): String {
//            // There is a tiny chance that the user will set the name of one of the existing
//            // watchlists to the name we generate here for a new watchlist.
//            // So, if only in theory, the constructor might throw.
//            val name = "Watchlist ${watchlist.hashCode()}"
//            register(watchlist, name)
//            return name
//        }

        private fun register(watchlist: Watchlist, name: String) {
            if (name in watchlists) {
                throw IllegalArgumentException("A watchlist named '$name' already exists.")
            }
            watchlists[name] = watchlist
        }

        fun deregister(watchlist: Watchlist) {
            watchlists.values.remove(watchlist)
        }

        operator fun get(key: String) = watchlists[key]

        fun disconnect() {
            watchlists.values.forEach {
                it.clearListeners()
                it.disconnect()
            }
        }

        fun save(): List<Settings> = watchlists.values.map {
            Settings(
                name = it.name,
                symbols = it.symbols
            )
        }

        fun restore(watchlists: List<Settings>) {
            watchlists.forEach {
                Watchlist(it.name, it.symbols)
            }
        }
    }

    private val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    private val map = mutableMapOf<String, Tops>()

    private val listeners = mutableListOf<ChangeListener>()

    fun addListener(listener: ChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ChangeListener) {
        listeners.remove(listener)
    }

    fun clearListeners() = listeners.clear()

    private val pendingAdd = mutableSetOf<String>()
    private val pendingRemove = mutableSetOf<String>()

    fun isPendingAdd(symbol: String) = symbol in pendingAdd
    fun isPendingRemove(symbol: String) = symbol in pendingRemove

    private fun updateMap(key: String, value: Tops?) {
        val oldValue = map[key]

        if (value != null)
            map[key] = value
        else
            map.remove(key)

        if (value != oldValue) {
            listeners.forEach {
                it(oldValue, value)
            }
        }
    }

    var name: String = "" // the setter is not triggered by the default value
        set(value) {
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

    init {
        this.name = name
        addSymbols(symbols)
        connect()
    }

    fun isConnected() = socket.connected()

    operator fun contains(key: String) = key in map
    operator fun get(key: String) = map[key]

    fun addSymbols(symbols: List<String>, ack: () -> Unit = {}): List<String> {
        val new = symbols
            .filter { it !in map && it !in pendingAdd && it.isNotBlank() }
            .map { it.trim() }

        if (new.isNotEmpty()) {
            pendingAdd.addAll(new)

            socket.connect()
            socket.emit("subscribe", arrayOf(new.joinToString(","))) {
                ack()
                println("Watchlist symbols subscribed: $it")
            }
        }

        return new
    }

    fun removeSymbols(symbols: List<String>, ack: () -> Unit = {}): List<String> {
        val old = symbols
            .filter { it in map && it.isNotBlank() }
            .map { it.trim() }

        if (old.isNotEmpty()) {
            pendingRemove.addAll(old)
            map.keys.removeAll(old)
            old.forEach {
                updateMap(it, null)
            }

            if (map.keys.isEmpty()) {
                // If no other threads are active, this will result in program exit.
                socket.disconnect()
            }
            socket.emit("unsubscribe", arrayOf(old.joinToString(","))) {
                ack()
                println("Watchlist symbols unsubscribed: $it")
            }
        }

        return old
    }

    fun connect() {
        socket
            .on(Socket.EVENT_MESSAGE) { params ->
                val tops = Iex.parseTops(params.first() as String)
                val symbol = tops.symbol
                if (symbol !in pendingRemove) {
                    updateMap(symbol, tops)
                    pendingAdd.remove(symbol)
                }
            }
            .on(Socket.EVENT_DISCONNECT) {
                appLogger.debug("Watchlist disconnected: ${map.keys}")
            }
    }

    fun disconnect() {
        socket.disconnect()
        socket.off() // Remove all listeners.
    }
}
