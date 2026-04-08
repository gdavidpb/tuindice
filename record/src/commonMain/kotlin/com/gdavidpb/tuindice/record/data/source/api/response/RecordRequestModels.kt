package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpsertAttemptOverrideRequest(
	@SerialName("score") val score: AttemptScoreRequest? = null,
	@SerialName("outcome") val outcome: OfficialOutcomeRequest? = null
)

@Serializable
data class AddSyntheticTermRequest(
	@SerialName("label") val label: String,
	@SerialName("start_at") val startAt: Long,
	@SerialName("end_at") val endAt: Long,
	@SerialName("attempts") val attempts: List<SyntheticAttemptRequest>
)

@Serializable
data class SyntheticAttemptRequest(
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	@SerialName("credits") val credits: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingModeRequest,
	@SerialName("score") val score: AttemptScoreRequest? = null,
	@SerialName("outcome") val outcome: OfficialOutcomeRequest? = null
)

@Serializable
data class AttemptScoreRequest(
	@SerialName("kind") val kind: AttemptScoreKindRequest,
	@SerialName("numeric_value") val numericValue: Int? = null,
	@SerialName("symbolic_value") val symbolicValue: String? = null
)

@Serializable
enum class AttemptScoreKindRequest {
	@SerialName("numeric")
	NUMERIC,

	@SerialName("symbolic")
	SYMBOLIC,

	@SerialName("empty")
	EMPTY
}

@Serializable
enum class AttemptGradingModeRequest {
	@SerialName("numeric")
	NUMERIC,

	@SerialName("qualitative_pass_fail")
	QUALITATIVE_PASS_FAIL
}

@Serializable
enum class OfficialOutcomeRequest {
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
