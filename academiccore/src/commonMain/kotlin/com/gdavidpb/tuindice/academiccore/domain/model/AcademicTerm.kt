package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicTerm(
	val id: String,
	@SerialName("period_year") val periodYear: Int,
	@SerialName("period_code") val periodCode: AcademicTermPeriod,
	@SerialName("term_kind") val kind: TermKind,
	val attempts: List<AcademicAttempt> = emptyList(),
	@SerialName("term_key") val termKey: String = "$periodYear-${periodCode.name}",
	@SerialName("term_order") val termOrder: Int = periodYear * 10 + periodCode.sequence,
	@SerialName("period_label") val periodLabel: String = "${periodCode.label} $periodYear",
	// The averages DST printed for a closed term. They anchor what a HISTORICAL term shows, because
	// that is the number on the student's constancia; the engine still computes its own. Absent on
	// CURRENT and SYNTHETIC terms, and on records the backend served before the anchor existed.
	@SerialName("official_period_average") val officialPeriodAverage: Double? = null,
	@SerialName("official_cumulative_average") val officialCumulativeAverage: Double? = null
)
