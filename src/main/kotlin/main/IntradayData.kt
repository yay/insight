package main

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private object Params {
    val symbol = "q"    // Stock symbol.
    val exchange = "x"  // Stock exchange symbol on which stock is traded (ex: NASD)
    val interval = "i"  // Interval size in seconds (86400 = 1 day intervals)
    val period = "p"    // Period. A number followed by a "d" or "Y", e.g. days or years. Ex: 40Y = 40 years.
    val format = "f"    // What data to fetch, comma separated, e.g.: d,c,v,o,h,l.
                        // Note: Column order may not match what you specify here.
                        // d - date - timestamp/interval
                        // c - close,
                        // v - volume
                        // o - open
                        // h - high
                        // l - low
    var start = "ts"    // Starting timestamp (Unix format). If blank, it uses today.
}

// Sample output:

/*

EXCHANGE%3DNASDAQ
MARKET_OPEN_MINUTE=570
MARKET_CLOSE_MINUTE=960
INTERVAL=60
COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
DATA=
TIMEZONE_OFFSET=-300
a1487082600,64.525,64.55,64.41,64.41,386437
1,64.41,64.72,64.36,64.4725,151569
2,64.51,64.54,64.4,64.41,130457
3,64.54,64.59,64.48,64.5,83536

*/

// One tricky bit with the first column (the date column) is the full and partial timestamps.
// The full timestamps are denoted by the leading 'a'. Like this: a1092945600.
// The number after the 'a' is a Unix timestamp. The numbers without a leading 'a' are "intervals".
// So, for example, the second row in the data set below has an interval of 1.
// You can multiply this number by our interval size (a minute, in this example) and add it to the
// last Unix Timestamp. That gives you the date for the current row. So our second row is 1 minute
// after the first row.


// https://www.google.com/finance/getprices?q=NVDA&x=NASD&i=120&p=25m&f=d,c,v,o,h,l&df=cpct&auto=1&ts=1488811864695&ei=7nW9WIiCNsGMUO-2htAE&authuser=0

// Fetch today's intra-day quotes for AAPL at 60 second interval:
// https://www.google.com/finance/getprices?q=AAPL&x=NASD&i=60&p=1d&f=d,c,v,o,h,l

class IntradayData(val symbol: String, val client: OkHttpClient = HttpClients.main) {
    private val baseUrl: String = "https://www.google.com/finance/getprices"
    private val urlBuilder = HttpUrl.parse(baseUrl).newBuilder()

    private var data: String = ""

    fun execute(): IntradayData {
        urlBuilder
                .addQueryParameter(Params.symbol, symbol)
                .addQueryParameter(Params.exchange, "NASD") // TODO: how to find out the exchange?
                .addQueryParameter(Params.interval, "60")
                .addQueryParameter(Params.period, "1d")
                .addQueryParameter(Params.format, "d,c,v,o,h,l")

        val url = urlBuilder.build()

        println(url)

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        response.use {
            if (it.isSuccessful) {
                data = it.body().string()
            } else {
                throw IOException( "$symbol request. Unexpected code: " + it )
            }
        }

        return this
    }

    fun data(): String { return data }
}