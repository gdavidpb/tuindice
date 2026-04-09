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
	@SerialName("official_score") val officialScore: AttemptScore = AttemptScore.empty(),
	@SerialName("official_outcome") val officialOutcome: AttemptOutcome = AttemptOutcome.PENDING,
	@SerialName("official_badge") val officialBadge: AttemptBadge = AttemptBadge.NONE
)
