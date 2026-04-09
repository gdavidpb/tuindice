package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermProjection(
	val id: String,
	val label: String,
	@SerialName("start_at") val startAtMillis: Long,
	@SerialName("end_at") val endAtMillis: Long,
	@SerialName("term_kind") val kind: TermKind,
	@SerialName("period_average") val periodAverage: Double,
	@SerialName("cumulative_average") val cumulativeAverage: Double,
	@SerialName("period_credits") val periodCredits: Int,
	@SerialName("cumulative_credits") val cumulativeCredits: Int,
	val attempts: List<AttemptProjection> = emptyList()
)
