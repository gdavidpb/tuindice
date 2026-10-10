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

	fun observeMutations(
		scopeKey: ScopeKey
	): Flow<List<MutationEnvelope<ScopeKey, Command>>>

	suspend fun getMutations(
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

	/**
	 * Moves every waiting row of the scope that expects an older revision up to [revision]: a
	 * newer one is known, and sending them with the old one would only earn a conflict each.
	 * Rows that already expect it or more are left alone. Returns how many were moved.
	 */
	suspend fun advancePendingRevisions(
		scopeKey: ScopeKey,
		revision: Long
	): Int = 0

	suspend fun requeueFailedMutations(
		scopeKey: ScopeKey,
		retryableBefore: Long
	): Int
}
