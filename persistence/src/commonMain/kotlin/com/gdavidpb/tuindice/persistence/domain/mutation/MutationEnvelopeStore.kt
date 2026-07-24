package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import kotlinx.coroutines.flow.Flow

interface MutationEnvelopeStore<ScopeKey, Command : OutboxMutation> {
	// Elegibles para envío (`Pending`).
	fun observePendingMutations(
		scopeKey: ScopeKey
	): Flow<List<MutationEnvelope<ScopeKey, Command>>>

	suspend fun getPendingMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>>

	// Todo lo que el usuario cambió y sigue guardado, sin importar el estado de envío:
	// el eje de visibilidad, separado del eje de elegibilidad de las dos de arriba.
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
}
