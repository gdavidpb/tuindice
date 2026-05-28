package com.gdavidpb.tuindice.base.domain.model

sealed class FlushPendingChangesResult {
	data object Success : FlushPendingChangesResult()

	data class PendingRemaining(
		val pendingChanges: PendingChanges
	) : FlushPendingChangesResult()

	data class OutdatedCredentials(
		val pendingChanges: PendingChanges
	) : FlushPendingChangesResult()
}
