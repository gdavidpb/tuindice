package com.gdavidpb.tuindice.base.utils.extension

fun Double.formatGrade(decimals: Int): String {
	return toFixed(decimals)
}

fun Float.formatGrade(decimals: Int): String {
	return toDouble().formatGrade(decimals)
}

private fun Double.toFixed(decimals: Int): String {
	if (decimals <= 0) return toInt().toString()

	val parts = toString().split('.')
	val integerPart = parts.firstOrNull().orEmpty()
	val decimalPart = parts.getOrNull(1).orEmpty()

	return "$integerPart.${decimalPart.padEnd(decimals, '0').take(decimals)}"
}
