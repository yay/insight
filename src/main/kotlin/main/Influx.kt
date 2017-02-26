package main

import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

fun loadFromDb(symbol: String) { //: List<main.OHLC> {
    val influx = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
    val dbName = "StockSeries"
    val query = Query("SELECT open, high, low, close FROM price", dbName)
    val result = influx.query(query)
    println(result)
}

fun saveToDb(symbol: String, data: List<OHLC>) {
    val influx = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
    val dbName = "EndOfDayQuotes"

    influx.createDatabase(dbName)

    val batchPoints = BatchPoints.database(dbName)
            .tag("symbol", symbol)
            .retentionPolicy("autogen")
            .consistency(InfluxDB.ConsistencyLevel.ALL)
            .build()

    for (ohlc in data) {
        val pricePoint = Point.measurement("price")
//            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//            .time(ZonedDateTime.now().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS)
                .time(ohlc.time, TimeUnit.MILLISECONDS)
                .addField("open", ohlc.open)
                .addField("high", ohlc.high)
                .addField("low", ohlc.low)
                .addField("close", ohlc.close)
                .addField("adjClose", ohlc.adjClose)
                .build()

        val volumePoint = Point.measurement("volume")
                .time(ohlc.time, TimeUnit.MILLISECONDS)
                .addField("volume", ohlc.volume)
                .build()

        batchPoints.point(pricePoint)
        batchPoints.point(volumePoint)
    }

    influx.write(batchPoints)

//    influx.deleteDatabase(dbName)
}

fun connectToDb() {
    val influx = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
    val dbName = "StockSeries"

    influx.createDatabase(dbName)

    val batchPoints = BatchPoints.database(dbName)
//            .tag("async", "true")
            .retentionPolicy("autogen")
            .consistency(InfluxDB.ConsistencyLevel.ALL)
            .build()

    val point1 = Point.measurement("cpu")
//            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .time(ZonedDateTime.now().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS)
            .addField("idle", 90L)
            .addField("user", 9L)
            .addField("system", 1L)
            .build()

    val point2 = Point.measurement("disk")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("used", 80L)
            .addField("free", 1L)
            .build()

    batchPoints.point(point1)
    batchPoints.point(point2)
    influx.write(batchPoints)
    val query = Query("SELECT idle FROM cpu", dbName)
    influx.query(query)

    influx.deleteDatabase(dbName)
}