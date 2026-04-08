package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicAttempt(
	val id: String,
	@SerialName("term_id") val termId: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	val credits: Int,
	@SerialName("sequence_in_term") val sequenceInTerm: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
	@SerialName("raw_grade_token") val rawGradeToken: String = "",
	@SerialName("raw_observation_text") val rawObservationText: String = "",
	@SerialName("official_score") val officialScore: AttemptScore = AttemptScore.empty(),
	@SerialName("official_outcome") val officialOutcome: OfficialOutcome = OfficialOutcome.PENDING,
	@SerialName("official_badge") val officialBadge: HistoricalBadge = HistoricalBadge.NONE,
	val source: AttemptSource
)
