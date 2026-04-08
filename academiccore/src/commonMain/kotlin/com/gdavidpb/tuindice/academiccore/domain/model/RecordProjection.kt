package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordProjection(
	@SerialName("view_mode") val viewMode: ProjectionViewMode,
	val terms: List<TermProjection> = emptyList()
)
