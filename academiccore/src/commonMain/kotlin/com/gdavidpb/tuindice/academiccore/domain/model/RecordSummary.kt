package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecordSummary(
	val official: ProjectionSummary = ProjectionSummary(),
	val simulation: ProjectionSummary = ProjectionSummary()
)
