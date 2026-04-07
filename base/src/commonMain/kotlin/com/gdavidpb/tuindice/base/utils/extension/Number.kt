package com.gdavidpb.tuindice.base.utils.extension

fun truncateScaledDivision(
    numerator: Long,
    denominator: Long,
    decimals: Int
): Double {
    if (denominator == 0L) return 0.0
    if (decimals <= 0) return (numerator / denominator).toDouble()

    val scale = decimalScale(decimals)
    val negative = (numerator < 0L) xor (denominator < 0L)
    val scaled = (kotlin.math.abs(numerator) * scale) / kotlin.math.abs(denominator)
    val normalized = scaled.toDouble() / scale.toDouble()

    return if (negative) -normalized else normalized
}

private fun decimalScale(decimals: Int): Long {
    var scale = 1L

    repeat(decimals) {
        scale *= 10L
    }

    return scale
}
