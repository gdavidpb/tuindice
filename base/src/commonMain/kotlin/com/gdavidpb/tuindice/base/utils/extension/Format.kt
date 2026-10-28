package com.gdavidpb.tuindice.base.utils.extension

// Grades are read to the hundredth, so they are written with two decimals even when the last one is zero (7.50, not
// 7.5).
const val GRADE_DECIMALS = 2

fun Double.formatGrade(decimals: Int): String {
	return toFixed(decimals)
}

fun Float.formatGrade(decimals: Int): String {
	return toDouble().formatGrade(decimals)
}

// A whole grade is written without decimals (8, not 8.00); any other one with the two decimals of the hundredth (7.50).
fun Double.formatCompactGrade(): String {
	return if (this == toInt().toDouble()) {
		formatGrade(decimals = 0)
	} else {
		formatGrade(decimals = GRADE_DECIMALS)
	}
}

private fun Double.toFixed(decimals: Int): String {
	if (decimals <= 0) return toInt().toString()

	val parts = toString().split('.')
	val integerPart = parts.firstOrNull().orEmpty()
	val decimalPart = parts.getOrNull(1).orEmpty()

	return "$integerPart.${decimalPart.padEnd(decimals, '0').take(decimals)}"
}
