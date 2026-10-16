package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import kotlinx.datetime.LocalDate

/**
 * The next evaluation of a subject that has no grade and has not gone by: [subjectCode] is the subject it belongs to,
 * and [type] and [ordinal] its name as the Evaluations tab gives it ("Parcial 2"); [date] is its day, which is today
 * when [isToday].
 */
data class SummaryNextEvaluation(
	val evaluationId: String,
	val subjectCode: String,
	val type: EvaluationType,
	val ordinal: Int,
	val date: LocalDate,
	val isToday: Boolean
)
