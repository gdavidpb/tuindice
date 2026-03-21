package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import kotlinx.coroutines.flow.Flow

interface MutationOutboxRepository<T : OutboxMutation> {
	fun observePendingMutations(): Flow<List<PendingMutation<T>>>
	suspend fun getPendingMutations(): List<PendingMutation<T>>
	suspend fun getPendingMutation(mutationId: String): PendingMutation<T>?
	suspend fun replacePendingMutation(mutation: PendingMutation<T>)
	suspend fun savePendingMutation(mutation: PendingMutation<T>)
	suspend fun deletePendingMutation(mutationId: String)
}
