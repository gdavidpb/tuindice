package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetSubjectGradeResponse(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("subject_patch") val subjectPatch: SubjectResponse,
	@SerialName("affected_quarters") val affectedQuarters: List<QuarterResponse>
)
