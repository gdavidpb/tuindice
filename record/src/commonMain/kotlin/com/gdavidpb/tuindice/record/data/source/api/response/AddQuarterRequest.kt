package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddQuarterRequest(
	@SerialName("quarter") val quarter: Int,
	@SerialName("year") val year: Int,
	@SerialName("subjects") val subjects: List<AddSubjectRequest>,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)
