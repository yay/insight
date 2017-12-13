package com.vitalyk.insight.dv

class OrdinalScale {
    var domain: Array<String> = arrayOf()
        set(value) {
            field = value
            rescale()
        }

    val range: Interval = 0.0 to 1.0

    val map: MutableMap<Any, Int> = mutableMapOf()

    fun rescale() {
        var index = 0

        for (datum in domain) {
            if (datum !in map) {
                map[datum] = index++
            }
        }
    }
}