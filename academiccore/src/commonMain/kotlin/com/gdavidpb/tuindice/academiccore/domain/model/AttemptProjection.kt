package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptProjection(
	val id: String,
	@SerialName("term_id") val termId: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	val credits: Int,
	@SerialName("sequence_in_term") val sequenceInTerm: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingMode,
	@SerialName("raw_grade_token") val rawGradeToken: String,
	@SerialName("raw_observation_text") val rawObservationText: String,
	val score: AttemptScore,
	val outcome: OfficialOutcome,
	val badge: HistoricalBadge,
	@SerialName("counts_toward_term_average") val countsTowardTermAverage: Boolean,
	@SerialName("counts_toward_cumulative_average") val countsTowardCumulativeAverage: Boolean
)
