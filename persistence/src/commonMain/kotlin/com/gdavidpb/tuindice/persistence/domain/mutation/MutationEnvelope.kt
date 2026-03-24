package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus

data class MutationEnvelope<ScopeKey, Command : OutboxMutation>(
	val mutationId: String,
	val scopeKey: ScopeKey,
	val command: Command,
	val precondition: MutationPrecondition,
	val status: PendingMutationStatus,
	val createdAt: Long,
	val updatedAt: Long,
	val lastError: String?,
	val replaceKey: String = command.replaceKey
) {
	val entityType: String
		get() = command.entityType

	val entityId: String
		get() = command.entityId

	val expectedRevision: Long?
		get() = (precondition as? MutationPrecondition.Revision)?.value
}
