package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicOverlay(
	@SerialName("attempt_overrides") val attemptOverrides: List<AttemptOverride> = emptyList(),
	@SerialName("synthetic_terms") val syntheticTerms: List<AcademicTerm> = emptyList()
)
