package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicTerm(
	val id: String,
	@SerialName("start_at") val startAtMillis: Long,
	@SerialName("end_at") val endAtMillis: Long,
	@SerialName("term_kind") val kind: TermKind,
	val attempts: List<AcademicAttempt> = emptyList()
)
