package com.gdavidpb.tuindice.base.domain.model

data class PendingChanges(
	val totalCount: Int,
	val recordCount: Int,
	val evaluationsCount: Int,
	val hasFailedMutations: Boolean
) {
	companion object {
		val Empty = PendingChanges(
			totalCount = 0,
			recordCount = 0,
			evaluationsCount = 0,
			hasFailedMutations = false
		)
	}
}
