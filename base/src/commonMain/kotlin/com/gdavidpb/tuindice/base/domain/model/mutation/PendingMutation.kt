package com.gdavidpb.tuindice.base.domain.model.mutation

data class PendingMutation<T : OutboxMutation>(
	val mutationId: String,
	val mutation: T,
	val expectedRevision: Long,
	val status: PendingMutationStatus,
	val createdAt: Long,
	val updatedAt: Long,
	val lastError: String?
)
