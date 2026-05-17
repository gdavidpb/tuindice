package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpsertAttemptOverrideRequest(
	@SerialName("score") val score: AttemptScore? = null,
	@SerialName("outcome") val outcome: AttemptOutcome? = null,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)
