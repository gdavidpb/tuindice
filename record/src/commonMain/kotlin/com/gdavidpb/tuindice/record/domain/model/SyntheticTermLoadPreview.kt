package com.gdavidpb.tuindice.record.domain.model

data class SyntheticTermLoadPreview(
	val available: Boolean,
	val reason: String? = null,
	val band: SyntheticTermLoadBand? = null,
	val credits: Int? = null,
	val weightedDifficulty: Double? = null,
	val loadIndex: Double? = null,
	val baselineLoadIndex: Double? = null,
	val effectiveTerms: Int? = null,
	val basis: SyntheticTermLoadBasis? = null,
	val confidence: SyntheticTermLoadConfidence? = null,
	val detail: SyntheticTermLoadDetail? = null
)
