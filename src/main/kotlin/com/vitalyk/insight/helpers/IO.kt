package com.vitalyk.insight.helpers

import java.io.File
import java.io.IOException

/**
 * Writes the string to a file with the specified `pathname`, creating all parent
 * directories in the process.
 * @param  pathname  A pathname string
 * @throws  SecurityException
 * @throws  IOException
 */
fun String.writeToFile(pathname: String) {
    val file = File(pathname)

    file.parentFile.mkdirs()
    file.writeText(this)
}