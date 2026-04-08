package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicRecordResponse(
	@SerialName("revision") val revision: Long,
	@SerialName("official_projection") val officialProjection: RecordProjectionResponse,
	@SerialName("simulation_projection") val simulationProjection: RecordProjectionResponse
)

@Serializable
data class RecordProjectionResponse(
	@SerialName("terms") val terms: List<TermProjectionResponse>
)

@Serializable
data class TermProjectionResponse(
	@SerialName("id") val id: String,
	@SerialName("label") val label: String,
	@SerialName("start_at") val startAt: Long,
	@SerialName("end_at") val endAt: Long,
	@SerialName("term_kind") val kind: TermKind,
	@SerialName("grade") val grade: Double,
	@SerialName("grade_sum") val gradeSum: Double,
	@SerialName("credits") val credits: Int,
	@SerialName("credits_sum") val creditsSum: Int,
	@SerialName("attempts") val attempts: List<AttemptProjectionResponse>
)

@Serializable
data class AttemptProjectionResponse(
	@SerialName("id") val id: String,
	@SerialName("term_id") val termId: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	@SerialName("credits") val credits: Int,
	@SerialName("sequence_in_term") val sequenceInTerm: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingModeResponse,
	@SerialName("score") val score: AttemptScoreResponse,
	@SerialName("outcome") val outcome: OfficialOutcomeResponse,
	@SerialName("badge") val badge: HistoricalBadgeResponse,
	@SerialName("editable") val editable: Boolean = false
)

@Serializable
data class AttemptScoreResponse(
	@SerialName("kind") val kind: AttemptScoreKindResponse,
	@SerialName("numeric_value") val numericValue: Int? = null,
	@SerialName("symbolic_value") val symbolicValue: String? = null
)

@Serializable
enum class AttemptScoreKindResponse {
	@SerialName("numeric")
	NUMERIC,

	@SerialName("symbolic")
	SYMBOLIC,

	@SerialName("empty")
	EMPTY
}

@Serializable
enum class AttemptGradingModeResponse {
	@SerialName("numeric")
	NUMERIC,

	@SerialName("qualitative_pass_fail")
	QUALITATIVE_PASS_FAIL
}

@Serializable
enum class OfficialOutcomeResponse {
	@SerialName("pending")
	PENDING,

	@SerialName("approved")
	APPROVED,

	@SerialName("failed")
	FAILED,

	@SerialName("retired")
	RETIRED,

	@SerialName("unreported")
	UNREPORTED
}

@Serializable
enum class HistoricalBadgeResponse {
	@SerialName("none")
	NONE,

	@SerialName("without_effect")
	WITHOUT_EFFECT
}
