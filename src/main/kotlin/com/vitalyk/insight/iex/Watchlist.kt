package com.vitalyk.insight.iex

import io.socket.client.IO
import io.socket.client.Socket
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import java.util.*
import com.vitalyk.insight.iex.IexApi.Tops as Tops

abstract class Alert<T> {
    var isActive = true
    abstract fun isTriggered(value: T): Boolean
}

class TopsAlert : Alert<Tops>() {

    override fun isTriggered(value: Tops): Boolean {
        return true
    }
}

class Watchlist {

    companion object {
        val watchlists = Collections.newSetFromMap( WeakHashMap<Watchlist, Boolean>() )

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
    }

    private val topsMap = FXCollections.observableHashMap<String, Tops>()
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
        get() = topsMap.keys.toList()

    constructor(symbols: List<String>) {
        add(symbols)
    }

    constructor(vararg symbols: String) {
        add(symbols.toList())
    }

    fun addListener(listener: MapChangeListener<String, Tops>) {
        topsMap.addListener(listener)
    }

    fun removeListener(listener: MapChangeListener<String, Tops>) {
        topsMap.removeListener(listener)
    }

    init {
        topsMap.keys.remove("BAC")
        topsMap.addListener(MapChangeListener { change ->
            println("added: ${change.valueAdded}")
            println("removed: ${change.valueRemoved}")
        })

        socket
            .on("message") { params ->
                val tops = IexApi.parseTops(params.first() as String)
                topsMap[tops.symbol] = tops

                println(tops)
            }
            .on(Socket.EVENT_DISCONNECT) {
                println("Watchlist disconnected.")
            }
    }

    fun isConnected() = socket.connected()

    operator fun contains(key: String) = key in topsMap
    operator fun get(key: String) = topsMap[key]

    fun add(symbols: List<String>) {
        val new = symbols.filter { it !in topsMap }
        if (new.isEmpty()) return

        socket.connect()
        socket.emit("subscribe", new.joinToString(","))
    }

    fun add(vararg symbols: String) {
        add(symbols.toList())
    }

    fun remove(symbols: List<String>) {
        val old = symbols.filter { it in topsMap }
        old.forEach { topsMap.remove(it) }

        if (topsMap.keys.isEmpty()) {
            // If no threads are running, this will result in program exit.
            socket.disconnect()
        }
        socket.emit("unsubscribe", old.joinToString(","))
    }

    fun remove(vararg symbols: String) {
        remove(symbols.toList())
    }

    fun clear() {
        topsMap.clear()
        socket.disconnect()
    }
}
