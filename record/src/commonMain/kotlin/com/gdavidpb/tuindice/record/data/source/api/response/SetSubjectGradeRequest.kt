package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetSubjectGradeRequest(
	@SerialName("grade") val grade: Int,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)
