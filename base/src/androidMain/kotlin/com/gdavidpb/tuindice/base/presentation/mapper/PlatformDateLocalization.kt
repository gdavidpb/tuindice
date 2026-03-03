package com.gdavidpb.tuindice.base.presentation.mapper

import java.text.DateFormatSymbols

actual fun platformFullMonthNames(): List<String> {
	return DateFormatSymbols()
		.months
		.take(12)
		.toList()
}

actual fun platformFullWeekdayNames(): List<String> {
	return DateFormatSymbols()
		.weekdays
		.toMondayFirstWeek()
}

actual fun platformShortWeekdayNames(): List<String> {
	return DateFormatSymbols()
		.shortWeekdays
		.toMondayFirstWeek()
}

private fun Array<String>.toMondayFirstWeek(): List<String> {
	return listOf(
		getOrElse(2) { "" },
		getOrElse(3) { "" },
		getOrElse(4) { "" },
		getOrElse(5) { "" },
		getOrElse(6) { "" },
		getOrElse(7) { "" },
		getOrElse(1) { "" }
	)
}
