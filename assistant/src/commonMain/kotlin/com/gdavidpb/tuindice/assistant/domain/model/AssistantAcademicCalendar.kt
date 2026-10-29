package com.gdavidpb.tuindice.assistant.domain.model

import kotlinx.datetime.LocalDate

// The weeks of the current term, as the app counts them. The assistant groups evaluations by these weeks but does not
// count them itself: the app's engine does, so the sheet reads the same weeks as the Evaluations tab.
interface AssistantAcademicCalendar {
	// The weeks the term has, in order. The Evaluations tab shows all of them, from the first to the last.
	val weeks: IntRange

	// The Monday that opens week 1 of the term.
	val weekOneStart: LocalDate

	// The week a date falls in, within [weeks]. A date before the first week counts as the first one and a date after the
	// last one as the last, so an evaluation does not disappear because of its date.
	fun weekOf(date: LocalDate): Int
}
