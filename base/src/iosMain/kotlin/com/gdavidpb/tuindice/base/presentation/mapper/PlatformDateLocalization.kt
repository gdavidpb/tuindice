package com.gdavidpb.tuindice.base.presentation.mapper

import platform.Foundation.NSDateFormatter

actual fun platformFullMonthNames(): List<String> {
	return localizedFormatter()
		.monthSymbols
		.map(::toLocalizedString)
}

actual fun platformFullWeekdayNames(): List<String> {
	return localizedFormatter()
		.weekdaySymbols
		.map(::toLocalizedString)
		.mondayFirst()
}

actual fun platformShortWeekdayNames(): List<String> {
	return localizedFormatter()
		.shortWeekdaySymbols
		.map(::toLocalizedString)
		.mondayFirst()
}

private fun localizedFormatter(): NSDateFormatter {
	return NSDateFormatter()
}

private fun List<String>.mondayFirst(): List<String> {
	if (isEmpty()) return emptyList()

	return drop(1) + first()
}

private fun toLocalizedString(value: Any?): String {
	return value?.toString().orEmpty()
}
