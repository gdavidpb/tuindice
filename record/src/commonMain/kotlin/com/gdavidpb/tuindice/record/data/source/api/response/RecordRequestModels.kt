package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
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
	@SerialName("period_year") val periodYear: Int,
	@SerialName("period_code") val periodCode: AcademicTermPeriod,
	@SerialName("subject_codes") val subjectCodes: List<String>,
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)

@Serializable
data class LoadSyntheticTermPreviewRequest(
	@SerialName("subject_codes") val subjectCodes: List<String>
)

@Serializable
data class SyntheticTermLoadPreviewResponse(
	@SerialName("available") val available: Boolean,
	@SerialName("reason") val reason: String? = null,
	@SerialName("band") val band: String? = null,
	@SerialName("label") val label: String? = null,
	@SerialName("credits") val credits: Int? = null,
	@SerialName("weighted_difficulty") val weightedDifficulty: Double? = null,
	@SerialName("load_index") val loadIndex: Double? = null,
	@SerialName("baseline_load_index") val baselineLoadIndex: Double? = null,
	@SerialName("effective_terms") val effectiveTerms: Int? = null
)

@Serializable
data class DeleteOverlayMutationRequest(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("expected_revision") val expectedRevision: Long
)
