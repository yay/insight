package com.vitalyk.dataviz.axis

import com.vitalyk.dataviz.scale.Scale

class Axis<D>(val orient: Orient, val scale: Scale<D, Double>) {
    enum class Orient {
        TOP, BOTTOM, LEFT, RIGHT;

        fun isVertical() = (this == LEFT || this == RIGHT)
        fun isHorizontal() = (this == TOP || this == BOTTOM)
    }
}