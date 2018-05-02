package com.vitalyk.insight.helpers

import java.util.zip.ZipInputStream

fun listResources(cls: Class<*>) {
    cls.protectionDomain.codeSource?.apply {
        val zip = ZipInputStream(location.openStream())
        while (true) {
            zip.nextEntry?.name?.let {
                println(it)
            } ?: break
        }
    }
}