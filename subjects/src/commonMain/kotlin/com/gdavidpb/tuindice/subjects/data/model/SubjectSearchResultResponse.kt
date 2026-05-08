package com.gdavidpb.tuindice.subjects.data.model

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectSearchResultResponse(
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("name") val name: String,
	@SerialName("credits") val credits: Int,
	@SerialName("grading_mode") val gradingMode: GradingMode
)
