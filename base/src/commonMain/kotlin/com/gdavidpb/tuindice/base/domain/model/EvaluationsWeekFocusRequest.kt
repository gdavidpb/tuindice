package com.gdavidpb.tuindice.base.domain.model

import kotlinx.datetime.LocalDate

/**
 * A request to show a week of the Evaluations tab: the Monday that opens it and, when the request
 * names one, the evaluation to bring into view inside that week. Two requests are the same only when
 * both fields are, so the consumer takes exactly the request it was shown and no other.
 */
data class EvaluationsWeekFocusRequest(
	val weekStart: LocalDate,
	val evaluationId: String? = null
)
