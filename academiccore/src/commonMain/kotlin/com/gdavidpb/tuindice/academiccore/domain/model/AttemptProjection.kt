package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptProjection(
	val id: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	val credits: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingMode,
	val score: AttemptScore,
	val outcome: AttemptOutcome,
	val badge: AttemptBadge
)
