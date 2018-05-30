package com.vitalyk.insight.helpers

/**
 * Given the desired `count` of elements in the returned list, calls the
 * `transform` on `step` groups of elements in the original list, where
 * `step` is calculated as `size / count` (integer division) and is at least 1.
 * Note, that the actual number of returned elements is likely to be greater than `count`.
 * The transform function is expected to produce a single element the represents
 * the average of the group of elements it received.
 */
fun <T, R> List<T>.aggregate(count: Int, transform: (List<T>) -> R): List<R> {
    if (count <= 0)
        throw IllegalArgumentException("'count' should be greater than zero.")
    val size = size
    val step: Int = (size / count).coerceAtLeast(1)
    val result = mutableListOf<R>()
    var i = 0
    while (i < size) {
        val mean = transform(slice(i until (i + step).coerceAtMost(size)))
        result.add(mean)
        i += step
    }
    result.reverse()
    return result
}