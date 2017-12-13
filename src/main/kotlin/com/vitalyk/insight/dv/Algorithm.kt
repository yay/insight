package com.vitalyk.insight.dv

import javafx.geometry.Point2D

fun Point2D.lineDistance(p1: Point2D, p2: Point2D): Double {
    val n = p2.subtract(p1).normalize()
    val pp1 = p1.subtract(this)

    return pp1.subtract(n.multiply(pp1.dotProduct(n))).magnitude()
}

// https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm

fun douglasPeucker(points: List<Point2D>, epsilon: Double): List<Point2D> {
    // Find the point furthest from the line connecting start and end points.
    var dmax = 0.0
    var index = 0
    val size = points.size
    val result: List<Point2D>

    for (i in 1..size - 2) {
        val d = points[i].lineDistance(points[0], points[size - 1])
        if (d > dmax) {
            index = i
            dmax = d
        }
    }
    // If the point is too far, recursively simplify.
    if (dmax > epsilon) {
        val result1 = douglasPeucker(points.slice(0..index), epsilon)
        val result2 = douglasPeucker(points.slice(index..size - 1), epsilon)
        result = result1.slice(0..result1.size - 2) + result2
    } else {
        // Furthest point is close enough for the whole sequence
        // to be represented by a line.
        result = listOf(points[0], points[size - 1])
    }
    return result
}

fun douglasPeuckerRange(points: List<Point2D>, range: IntRange, epsilon: Double): List<Int> {
    var dmax = 0.0
    var index = range.start
    val result: List<Int>

    for (i in range.start + 1..range.endInclusive - 1) {
        val d = points[i].lineDistance(points[range.start], points[range.endInclusive])
        if (d > dmax) {
            index = i
            dmax = d
        }
    }
    if (dmax > epsilon) {
        val result1 = douglasPeuckerRange(points, range.start..index, epsilon)
        val result2 = douglasPeuckerRange(points, index..range.endInclusive, epsilon)
        result = result1.take(result1.size - 1) + result2
    } else {
        result = listOf(0, points.size - 1)
    }
    return result
}