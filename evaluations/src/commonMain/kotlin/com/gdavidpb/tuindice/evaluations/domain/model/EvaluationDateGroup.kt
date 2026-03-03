package com.gdavidpb.tuindice.evaluations.domain.model

import kotlinx.datetime.LocalDate

sealed interface EvaluationDateGroup {
	data object Continuous : EvaluationDateGroup
	data object Today : EvaluationDateGroup
	data object Tomorrow : EvaluationDateGroup
	data object Yesterday : EvaluationDateGroup
	data class PastThisWeek(val date: LocalDate) : EvaluationDateGroup
	data class ThisWeek(val date: LocalDate) : EvaluationDateGroup
	data class NextWeek(val date: LocalDate) : EvaluationDateGroup
	data class WeeksAhead(val weeks: Long) : EvaluationDateGroup
	data class ExactDate(val date: LocalDate) : EvaluationDateGroup
}
