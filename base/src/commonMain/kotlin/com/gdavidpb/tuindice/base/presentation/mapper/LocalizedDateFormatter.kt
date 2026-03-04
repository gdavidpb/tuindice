package com.gdavidpb.tuindice.base.presentation.mapper

import kotlinx.datetime.LocalDate

private val FULL_MONTH_NAMES = listOf(
	"enero",
	"febrero",
	"marzo",
	"abril",
	"mayo",
	"junio",
	"julio",
	"agosto",
	"septiembre",
	"octubre",
	"noviembre",
	"diciembre"
)

private val FULL_WEEKDAY_NAMES = listOf(
	"lunes",
	"martes",
	"miércoles",
	"jueves",
	"viernes",
	"sábado",
	"domingo"
)

private val SHORT_WEEKDAY_NAMES = listOf(
	"lun",
	"mar",
	"mié",
	"jue",
	"vie",
	"sáb",
	"dom"
)

fun LocalDate.formatLocalizedMonthYear(): String {
	return "${localizedFullMonthNames()[month.ordinal]} $year"
}

fun localizedShortWeekdayNames(): List<String> {
	return SHORT_WEEKDAY_NAMES
}

fun localizedFullWeekdayNames(): List<String> {
	return FULL_WEEKDAY_NAMES
}

fun localizedFullMonthNames(): List<String> {
	return FULL_MONTH_NAMES
}
