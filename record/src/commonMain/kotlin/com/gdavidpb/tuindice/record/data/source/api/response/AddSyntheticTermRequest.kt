package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddSyntheticTermRequest(
	@SerialName("period_year") val periodYear: Int,
	@SerialName("period_code") val periodCode: AcademicTermPeriod,
	@SerialName("subject_codes") val subjectCodes: List<String>,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)
