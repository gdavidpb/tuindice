package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicRecord(
	val id: String,
	val profile: AcademicProfile = AcademicProfile(),
	val terms: List<AcademicTerm> = emptyList(),
	@SerialName("attempt_overrides") val attemptOverrides: List<AttemptOverride> = emptyList()
)
