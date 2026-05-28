package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermProjection(
	val id: String,
	@SerialName("period_year") val periodYear: Int,
	@SerialName("period_code") val periodCode: AcademicTermPeriod,
	@SerialName("term_key") val termKey: String,
	@SerialName("term_order") val termOrder: Int,
	@SerialName("period_label") val periodLabel: String,
	@SerialName("term_kind") val kind: TermKind,
	@SerialName("period_average") val periodAverage: Double,
	@SerialName("cumulative_average") val cumulativeAverage: Double,
	@SerialName("period_credits") val periodCredits: Int,
	@SerialName("cumulative_credits") val cumulativeCredits: Int,
	val attempts: List<AttemptProjection> = emptyList()
)
