package main

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.joda.time.DateTime
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import java.io.File
import java.math.BigDecimal
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat

object DB {

    lateinit var insight: DBI

    fun connect(): Boolean {
        try {
            insight = DBI("jdbc:postgresql://localhost:5432/insight")
        } catch (e: SQLException) {
            getAppLogger().error(e.message)
            return false
        }

        return true
    }
}

fun runDbMigration(db: DBI) {
    val runner = MigrationRunner(db)
}

fun createTableIndex(db: DBI) {
    db.useHandle {
        it.execute("begin")
        it.execute("alter table dailyquotes add primary key (quote_date, market, symbol)")
        it.execute("commit")
    }
}

private fun BigDecimal.isValidPrice(): Boolean = this.precision() <= 8 && this.scale() <= 6
private fun String.isValidSymbol(): Boolean = this.length <= 6
private fun String.isValidMarket(): Boolean = this.length <= 4

/**
 * @param  handle  Database connection
 * @param  records  A parser over a stream of records (Iterable)
 * @param  market  Exchange code
 * @param  symbol  Ticker symbol
 * @param  dateFormat  Date format to use for parsing date column values
 */
fun csvTickerDailyQuotesToDb(handle: Handle, records: CSVParser,
                 market: String, symbol: String, dateFormat: SimpleDateFormat): Boolean {

    records.forEach {
        val rec = it
        val date = DateTime(dateFormat.parse(rec.get(YahooDataColumns.date)))
        val open = BigDecimal(rec.get(YahooDataColumns.open))
        val high = BigDecimal(rec.get(YahooDataColumns.high))
        val low = BigDecimal(rec.get(YahooDataColumns.low))
        val close = BigDecimal(rec.get(YahooDataColumns.close))
        val adjClose = BigDecimal(rec.get(YahooDataColumns.adjClose))
        val volume = rec.get(YahooDataColumns.volume).toLong()

        if (adjClose.isValidPrice() && // adjClose check is most likely to fail so it goes first
                open.isValidPrice() &&
                high.isValidPrice() &&
                low.isValidPrice() &&
                close.isValidPrice() &&
                symbol.isValidSymbol() &&
                market.isValidMarket()) {

            try {
                handle.createStatement("insert into dailyquotes" +
                        " (quote_date, market, symbol, \"open\", high, low, \"close\", adj_close, volume)" +
                        " values (:quote_date, :market, :symbol, :open, :high, :low, :close, :adj_close, :volume)")
                        .bind("quote_date", Timestamp(date.millis))
                        .bind("market", market)
                        .bind("symbol", symbol)
                        .bind("open", open)
                        .bind("high", high)
                        .bind("low", low)
                        .bind("close", close)
                        .bind("adj_close", adjClose)
                        .bind("volume", volume)
                        .execute()
            } catch (e: Exception) {
                getAppLogger().error("Exception on ($date, $symbol, $market): $e\n" +
                        "$symbol data will not be added to the database.")
                handle.execute("rollback")
                return false
            }

        }

    }

    return true
}

val exchangeToMarketMap = mapOf(
        "nasdaq" to "XNAS",
        "nyse" to "XNYS",
        "amex" to "XASE"
)

fun csvAllDailyQuotesToDb(db: DBI) {

    val basePath = "${AppSettings.paths.storage}/${AppSettings.paths.dailyData}/24-02-2017/"

    db.useHandle {

        // IMPORTANT:
        // https://www.postgresql.org/docs/9.1/static/populate.html

        val handle = it
        var index = 0

        for ((exchange, market) in exchangeToMarketMap) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val path = basePath + exchange
            val walker = File(path).walk().maxDepth(1)

            for (file in walker) {
                val symbol = file.nameWithoutExtension.trim()
                if (symbol != exchange) {
                    getAppLogger().debug("${index++} - $market:$symbol")
                    val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file.reader())

                    handle.execute("begin")

                    if (csvTickerDailyQuotesToDb(handle, records, market, symbol, dateFormat)) {
                        handle.execute("commit")
                    } else {
                        handle.execute("rollback")
                    }
                }
            }
        }

    }
}