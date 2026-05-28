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
	@SerialName("period_label") val periodLabel: String = "${periodCode.label} $periodYear"
)
