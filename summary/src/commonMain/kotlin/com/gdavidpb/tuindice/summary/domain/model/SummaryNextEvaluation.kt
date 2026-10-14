package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import kotlinx.datetime.LocalDate

/**
 * The next evaluation of a subject that has no grade and has not gone by: [type] and [ordinal] are its name as the
 * Evaluations tab gives it ("Parcial 2"), and [date] its day, which is today when [isToday].
 */
data class SummaryNextEvaluation(
	val evaluationId: String,
	val type: EvaluationType,
	val ordinal: Int,
	val date: LocalDate,
	val isToday: Boolean
)
