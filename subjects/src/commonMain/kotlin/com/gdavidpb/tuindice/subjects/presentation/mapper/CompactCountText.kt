package com.gdavidpb.tuindice.subjects.presentation.mapper

import kotlin.math.roundToInt

fun Int.toCompactCountText(): String {
	if (this < 1000) return toString()

	val scaledTenths = ((this / 1000.0) * 10).roundToInt()
	val wholePart = scaledTenths / 10
	val decimalPart = scaledTenths % 10

	return if (decimalPart == 0) {
		"${wholePart}k"
	} else {
		"$wholePart.${decimalPart}k"
	}
}
