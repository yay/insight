package com.vitalyk.insight.iex

import com.vitalyk.insight.iex.Iex.Tops
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.collections.List
import kotlin.collections.contains
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.set
import kotlin.collections.toList

typealias ChangeListener = (old: Tops?, new: Tops?) -> Unit

class Watchlist(name: String, symbols: List<String> = emptyList()) {

    data class Settings(
        val name: String,
        val symbols: List<String>
    )

    companion object {
        private val logger = LoggerFactory.getLogger(Watchlist::class.java)

        private val watchlists = mutableMapOf<String, Watchlist>()

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

        fun getOrPut(name: String) = Watchlist[name] ?: Watchlist(name)
    }

    private val socket = IO.socket("https://ws-api.iextrading.com/1.0/tops")

    private val map = mutableMapOf<String, Tops>()

    private val listeners = mutableSetOf<ChangeListener>()

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

        if (value != oldValue) {
            if (value != null)
                map[key] = value
            else
                map.remove(key)

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
//        simulation()
    }

    fun isConnected() = socket.connected()

    operator fun contains(key: String) = key in map
    operator fun get(key: String) = map[key]

    fun addSymbols(symbols: List<String>, ack: () -> Unit = {}): List<String> {
        val new = symbols
            .filter { it.isNotBlank() && it !in map && it !in pendingAdd }
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
            .filter { it.isNotBlank() && it in map }
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

    private var connectTime: Instant? = null

    fun connect() {
        connectTime = Instant.now()
        socket
            .on(Socket.EVENT_MESSAGE) { params ->
                val tops = Iex.parseTops(params.first() as String)
                val symbol = tops.symbol
                // https://github.com/iexg/IEX-API/issues/307
                updateMap(symbol, tops)
                pendingAdd.remove(symbol)
            }
            .on(Socket.EVENT_DISCONNECT) {
                logger.debug("Watchlist disconnected: ${map.keys}")
                val minutesConnected = Duration.between(connectTime, Instant.now()).toMinutes()
                socket.off()
                connect()
                throw IOException("Watchlist $name disconnected. Connected for $minutesConnected minutes. Reconnecting...")
            }
    }

    /*
    java.lang.Error: java.io.IOException: Watchlist Main disconnected.
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1155)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.io.IOException: Watchlist Main disconnected.
	at com.vitalyk.insight.iex.Watchlist$connect$2.call(Watchlist.kt:179)
	at io.socket.emitter.Emitter.emit(Emitter.java:117)
	at io.socket.client.Socket.access$601(Socket.java:24)
	at io.socket.client.Socket$5.run(Socket.java:186)
	at io.socket.thread.EventThread.exec(EventThread.java:55)
	at io.socket.client.Socket.emit(Socket.java:182)
	at io.socket.client.Socket.onclose(Socket.java:275)
	at io.socket.client.Socket.access$200(Socket.java:24)
	at io.socket.client.Socket$2$3.call(Socket.java:126)
	at io.socket.emitter.Emitter.emit(Emitter.java:117)
	at io.socket.client.Manager.onclose(Manager.java:553)
	at io.socket.client.Manager.access$1500(Manager.java:30)
	at io.socket.client.Manager$6.call(Manager.java:397)
	at io.socket.emitter.Emitter.emit(Emitter.java:117)
	at io.socket.engineio.client.Socket.onClose(Socket.java:862)
	at io.socket.engineio.client.Socket.onError(Socket.java:821)
	at io.socket.engineio.client.Socket.access$900(Socket.java:36)
	at io.socket.engineio.client.Socket$4.call(Socket.java:340)
	at io.socket.emitter.Emitter.emit(Emitter.java:117)
	at io.socket.engineio.client.Transport.onError(Transport.java:64)
	at io.socket.engineio.client.transports.WebSocket.access$400(WebSocket.java:24)
	at io.socket.engineio.client.transports.WebSocket$1$5.run(WebSocket.java:107)
	at io.socket.thread.EventThread$2.run(EventThread.java:80)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	... 2 more

     */

    fun simulation() {
        launch {
            while (isActive) {
                delay(1500)
                map.values.forEach { oldTop ->
                    if (Math.random() > 0.5) {
                        val spread = 0.1 + Math.random()
                        val bid = oldTop.bidPrice - 0.5 + Math.random() * 1.0
                        val ask = bid + spread
                        val top = oldTop.copy(
                            lastSalePrice = bid + Math.random() * spread,
                            bidPrice = bid,
                            askPrice = ask
                        )
                        updateMap(top.symbol, top)
                    }
                }
            }
        }
    }

    fun disconnect() {
        socket.disconnect()
        socket.off() // Remove all listeners.
    }
}
