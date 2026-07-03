package com.gdavidpb.tuindice.record.data.model

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSyntheticTermSubjectSearchResponse(
	@SerialName("results") val results: List<Result> = emptyList()
) {
	@Serializable
	internal data class Result(
		@SerialName("subject_code") val subjectCode: String,
		@SerialName("name") val name: String,
		@SerialName("credits") val credits: Int,
		@SerialName("grading_mode") val gradingMode: GradingMode? = GradingMode.NUMERIC
	)
}
