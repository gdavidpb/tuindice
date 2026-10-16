package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import kotlinx.datetime.LocalDate

/**
 * What the date dialog knows about the evaluation it picks a date for: the days it allows (null is
 * unbounded: no current term) and, when a subject is chosen, its code and the days of its class among the
 * allowed ones, each with the place of that class. A dot goes under each class day, and the chosen day says
 * where its class is. The dialog looks a date up in these; it does not work the class days out again.
 */
data class EvaluationDateCalendarItem(
	val selectableRange: ClosedRange<LocalDate>? = null,
	val subjectCode: String? = null,
	val classDates: Set<LocalDate> = emptySet(),
	val classPlaces: Map<LocalDate, ClassPlaceItem> = emptyMap()
)
