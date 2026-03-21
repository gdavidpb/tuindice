package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.base.domain.repository.MutationOutboxRepository
import com.gdavidpb.tuindice.persistence.data.room.mapper.toPendingMutation
import com.gdavidpb.tuindice.persistence.data.room.mapper.toPendingMutationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class RoomMutationOutboxRepository<T : OutboxMutation>(
	private val room: TuIndiceDatabase,
	private val serializer: KSerializer<T>,
	private val json: Json = Json
) : MutationOutboxRepository<T> {
	override fun observePendingMutations(): Flow<List<PendingMutation<T>>> {
		return room.pendingMutations.observePendingMutations()
			.map { entities -> entities.map { entity -> entity.toPendingMutation(serializer, json) } }
	}

	override suspend fun getPendingMutations(): List<PendingMutation<T>> {
		return room.pendingMutations.getPendingMutations()
			.map { entity -> entity.toPendingMutation(serializer, json) }
	}

	override suspend fun getPendingMutation(mutationId: String): PendingMutation<T>? {
		return room.pendingMutations.getPendingMutation(mutationId)
			?.toPendingMutation(serializer, json)
	}

	override suspend fun replacePendingMutation(mutation: PendingMutation<T>) {
		room.withImmediateTransaction {
			room.pendingMutations.deletePendingMutationsByReplaceKey(mutation.mutation.replaceKey)
			room.pendingMutations.upsertEntity(mutation.toPendingMutationEntity(serializer, json))
		}
	}

	override suspend fun savePendingMutation(mutation: PendingMutation<T>) {
		room.pendingMutations.upsertEntity(mutation.toPendingMutationEntity(serializer, json))
	}

	override suspend fun deletePendingMutation(mutationId: String) {
		room.pendingMutations.deletePendingMutation(mutationId)
	}
}
