package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectionViewMode {
	@SerialName("official")
	OFFICIAL,

	@SerialName("simulation")
	SIMULATION
}
