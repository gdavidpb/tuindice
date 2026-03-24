package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.persistence.data.room.mapper.toMutationEnvelope
import com.gdavidpb.tuindice.persistence.data.room.mapper.toPendingMutationEntity
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class RoomMutationEnvelopeStore<ScopeKey, Command : OutboxMutation>(
	private val room: TuIndiceDatabase,
	private val storeId: String,
	private val scopeKeySerializer: KSerializer<ScopeKey>,
	private val commandSerializer: KSerializer<Command>,
	private val json: Json = Json
) : MutationEnvelopeStore<ScopeKey, Command> {
	override fun observePendingMutations(
		scopeKey: ScopeKey
	) = room.pendingMutations.observePendingMutations(
		storeId = storeId,
		scopeKey = json.encodeToString(scopeKeySerializer, scopeKey)
	).map { entities ->
		entities.map { entity ->
			entity.toMutationEnvelope(
				scopeKeySerializer = scopeKeySerializer,
				commandSerializer = commandSerializer,
				json = json
			)
		}
	}

	override suspend fun getPendingMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>> {
		return room.pendingMutations.getPendingMutations(
			storeId = storeId,
			scopeKey = json.encodeToString(scopeKeySerializer, scopeKey)
		).map { entity ->
			entity.toMutationEnvelope(
				scopeKeySerializer = scopeKeySerializer,
				commandSerializer = commandSerializer,
				json = json
			)
		}
	}

	override suspend fun getPendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	): MutationEnvelope<ScopeKey, Command>? {
		return room.pendingMutations.getPendingMutation(
			storeId = storeId,
			scopeKey = json.encodeToString(scopeKeySerializer, scopeKey),
			mutationId = mutationId
		)?.toMutationEnvelope(
			scopeKeySerializer = scopeKeySerializer,
			commandSerializer = commandSerializer,
			json = json
		)
	}

	override suspend fun replacePendingMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	) {
		room.withImmediateTransaction {
			room.pendingMutations.deletePendingMutationsByReplaceKey(
				storeId = storeId,
				replaceKey = mutation.replaceKey
			)
			room.pendingMutations.upsertEntity(
				mutation.toPendingMutationEntity(
					storeId = storeId,
					scopeKeySerializer = scopeKeySerializer,
					commandSerializer = commandSerializer,
					json = json
				)
			)
		}
	}

	override suspend fun savePendingMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	) {
		room.pendingMutations.upsertEntity(
			mutation.toPendingMutationEntity(
				storeId = storeId,
				scopeKeySerializer = scopeKeySerializer,
				commandSerializer = commandSerializer,
				json = json
			)
		)
	}

	override suspend fun deletePendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	) {
		room.pendingMutations.deletePendingMutation(
			storeId = storeId,
			scopeKey = json.encodeToString(scopeKeySerializer, scopeKey),
			mutationId = mutationId
		)
	}
}
