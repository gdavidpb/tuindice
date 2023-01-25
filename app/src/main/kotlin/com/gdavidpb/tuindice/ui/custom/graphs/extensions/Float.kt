package com.gdavidpb.tuindice.ui.custom.graphs.extensions

import kotlin.math.abs

fun Float.equals(x: Float, epsilon: Float): Boolean {
    val d = this / x
    return abs(d - 1.0f) < epsilon
}

fun ClosedFloatingPointRange<Float>.contains(x: Float, epsilon: Float): Boolean {
    return !x.equals(start, epsilon) && !x.equals(endInclusive, epsilon) && x in this
}