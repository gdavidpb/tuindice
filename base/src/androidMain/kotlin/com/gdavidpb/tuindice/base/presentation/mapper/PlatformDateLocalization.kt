package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.utils.ANDROID_LOCALE_LANGUAGE_TAG
import java.text.DateFormatSymbols
import java.util.Locale

actual fun platformFullMonthNames(): List<String> {
	return DateFormatSymbols(appLocale())
		.months
		.take(12)
		.toList()
}

actual fun platformFullWeekdayNames(): List<String> {
	return DateFormatSymbols(appLocale())
		.weekdays
		.toMondayFirstWeek()
}

actual fun platformShortWeekdayNames(): List<String> {
	return DateFormatSymbols(appLocale())
		.shortWeekdays
		.toMondayFirstWeek()
}

private fun appLocale(): Locale {
	return Locale.forLanguageTag(ANDROID_LOCALE_LANGUAGE_TAG)
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
