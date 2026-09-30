package com.gdavidpb.tuindice.pensum.data.model

/**
 * The cached pensum the student currently looks at: which year and modality it is, whether that
 * selection was inferred by the backend rather than chosen, and when it was last fetched.
 */
data class SelectedPensumCacheState(
	val year: Int?,
	val modalityId: String?,
	val inferred: Boolean,
	val updatedAt: Long
)
