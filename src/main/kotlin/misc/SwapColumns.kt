package misc

import main.AppSettings
import java.io.File

fun swapVolumeAndAdjCloseColumnsInDailyData() {
    val basePath = "${AppSettings.paths.dailyData}/"
    val outBasePath = "${AppSettings.paths.storage}/new_stock_daily"

    val exchanges = listOf("amex", "nasdaq", "nyse")

    for (exchange in exchanges) {
        val path = basePath + exchange
        val walker = File(path).walk().maxDepth(1)

        for (file in walker) {
            if (file.isFile()) {
                val symbol = file.nameWithoutExtension.trim()
                if (symbol != exchange) {
                    val lines = file.readLines()
                    val newLines = mutableListOf<String>()
                    for (line in lines) {
                        val values = line.split(",")
                        if (values.count() == 7) {
                            newLines.add(values[0] + "," + values[1] + "," + values[2] + "," + values[3]
                                + "," + values[4] + "," + values[6] + "," + values[5])
                        } else {
                            println("$exchange:$symbol error: each row should have 7 columns," +
                                " ${values.count()} found.")
                            break;
                        }
                    }
                    if (newLines.count() > 0) {
                        val outPath = "$outBasePath/$exchange/$symbol.csv"
                        val outFile = File(outPath)
                        outFile.parentFile.mkdirs()
                        outFile.writeText(newLines.joinToString("\n"))
                    } else {
                        println("$exchange:$symbol error: no lines to write.")
                    }
                }
            }
        }
    }
}
