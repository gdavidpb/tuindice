package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicTerm(
	val id: String,
	val label: String,
	@SerialName("start_at") val startAtMillis: Long,
	@SerialName("end_at") val endAtMillis: Long,
	val order: Int,
	@SerialName("term_kind") val kind: TermKind,
	@SerialName("source_reported_period_average")
	val sourceReportedPeriodAverage: Double? = null,
	@SerialName("source_reported_cumulative_average")
	val sourceReportedCumulativeAverage: Double? = null,
	val attempts: List<AcademicAttempt> = emptyList()
)
