package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OfficialOutcome {
	@SerialName("pending")
	PENDING,

	@SerialName("approved")
	APPROVED,

	@SerialName("failed")
	FAILED,

	@SerialName("retired")
	RETIRED,

	@SerialName("unreported")
	UNREPORTED
}
