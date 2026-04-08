package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetSubjectGradeRequest(
	@SerialName("grade") val grade: Int? = null,
	@SerialName("status") val status: SubjectStatus? = null,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)
