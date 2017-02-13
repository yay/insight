import tornadofx.getProperty
import tornadofx.property
import java.util.*

open class StockSymbol(date: Date, open: Float, high: Float, low: Float, close: Float, volume: Float, adjClose: Float) {
    var date: Date by property(date)
    fun dateProperty() = getProperty(StockSymbol::date)

    var open: Float by property(open)
    fun openProperty() = getProperty(StockSymbol::open)

    var high: Float by property(high)
    fun highProperty() = getProperty(StockSymbol::high)

    var low: Float by property(low)
    fun lowProperty() = getProperty(StockSymbol::low)

    var close: Float by property(close)
    fun closeProperty() = getProperty(StockSymbol::close)

    var volume: Float by property(volume)
    fun volumeProperty() = getProperty(StockSymbol::volume)

    var adjClose: Float by property(adjClose)
    fun adjCloseProperty() = getProperty(StockSymbol::adjClose)
}