package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicSnapshot(
	@SerialName("terms") val terms: List<AcademicTerm> = emptyList()
)
