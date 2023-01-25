package com.gdavidpb.tuindice.base.utils.extension

import kotlin.math.floor
import kotlin.math.pow

fun Double.round(decimals: Int): Double {
    val pow = 10.0.pow(decimals)

    return floor(this * pow) / pow
}