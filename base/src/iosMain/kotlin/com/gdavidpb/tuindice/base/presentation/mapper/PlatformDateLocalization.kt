package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.utils.IOS_LOCALE_LANGUAGE_TAG
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

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
	return NSDateFormatter().apply {
		locale = NSLocale(localeIdentifier = IOS_LOCALE_LANGUAGE_TAG)
	}
}

private fun List<String>.mondayFirst(): List<String> {
	if (isEmpty()) return emptyList()

	return drop(1) + first()
}

private fun toLocalizedString(value: Any?): String {
	return value?.toString().orEmpty()
}
