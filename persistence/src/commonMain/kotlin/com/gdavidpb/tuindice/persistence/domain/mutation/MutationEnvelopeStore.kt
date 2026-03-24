package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import kotlinx.coroutines.flow.Flow

interface MutationEnvelopeStore<ScopeKey, Command : OutboxMutation> {
	fun observePendingMutations(
		scopeKey: ScopeKey
	): Flow<List<MutationEnvelope<ScopeKey, Command>>>

	suspend fun getPendingMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>>

	suspend fun getPendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	): MutationEnvelope<ScopeKey, Command>?

	suspend fun replacePendingMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	)

	suspend fun savePendingMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	)

	suspend fun deletePendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	)
}
