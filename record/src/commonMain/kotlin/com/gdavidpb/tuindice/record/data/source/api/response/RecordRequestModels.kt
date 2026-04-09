package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpsertAttemptOverrideRequest(
	@SerialName("score") val score: AttemptScore? = null,
	@SerialName("outcome") val outcome: AttemptOutcome? = null,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)

@Serializable
data class AddSyntheticTermRequest(
	@SerialName("label") val label: String,
	@SerialName("start_at") val startAt: Long,
	@SerialName("end_at") val endAt: Long,
	@SerialName("attempts") val attempts: List<SyntheticAttemptRequest>,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)

@Serializable
data class DeleteOverlayMutationRequest(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)

@Serializable
data class SyntheticAttemptRequest(
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("subject_name") val subjectName: String,
	@SerialName("credits") val credits: Int,
	@SerialName("grading_mode") val gradingMode: AttemptGradingMode,
	@SerialName("score") val score: AttemptScore? = null,
	@SerialName("outcome") val outcome: AttemptOutcome? = null
)
