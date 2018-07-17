package com.vitalyk.viz.scale

interface Scale<D, R> {
    val domain: List<D>
    val range: List<R>

    fun domain(d: List<D>)
    fun range(r: List<R>)

    operator fun invoke(d: D): R

    fun ticks(count: Int = 10): List<D>
}