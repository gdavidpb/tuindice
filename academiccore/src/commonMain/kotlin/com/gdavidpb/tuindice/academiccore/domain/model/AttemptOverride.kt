package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptOverride(
	@SerialName("attempt_id") val attemptId: String,
	val score: AttemptScore? = null,
	val outcome: AttemptOutcome? = null,
	@SerialName("updated_at") val updatedAtMillis: Long
) {
	init {
		require(score != null || outcome != null) { "AttemptOverride requires a score or outcome." }
	}
}
