package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
	@SerialName("effective_terms") val effectiveTerms: Int? = null,
	@SerialName("basis") val basis: String? = null,
	@SerialName("confidence") val confidence: String? = null,
	@SerialName("detail") val detail: String? = null
)
