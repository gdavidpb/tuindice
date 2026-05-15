package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicAttempt(
	val id: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	val credits: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
	@SerialName("academic_score") val academicScore: AttemptScore = AttemptScore.empty(),
	@SerialName("academic_outcome") val academicOutcome: AttemptOutcome = AttemptOutcome.PENDING,
	@SerialName("academic_badge") val academicBadge: AttemptBadge = AttemptBadge.NONE
)
