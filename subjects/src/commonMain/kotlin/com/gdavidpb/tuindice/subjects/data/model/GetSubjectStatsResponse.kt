package com.gdavidpb.tuindice.subjects.data.model

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetSubjectStatsResponse(
	@SerialName("id") val id: String,
	@SerialName("name") val name: String? = null,
	@SerialName("credits") val credits: Int? = null,
	@SerialName("grading_mode") val gradingMode: GradingMode? = null,
	@SerialName("generated_at") val generatedAt: Long,
	@SerialName("expires_at") val expiresAt: Long,
	@SerialName("career_segment") val careerSegment: SubjectStatsSegmentResponse? = null,
	@SerialName("global_segment") val globalSegment: SubjectStatsSegmentResponse? = null
)
