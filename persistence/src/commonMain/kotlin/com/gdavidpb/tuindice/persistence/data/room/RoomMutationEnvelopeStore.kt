package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.mapper.toMutationEnvelope
import com.gdavidpb.tuindice.persistence.data.room.mapper.toPendingMutationEntity
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class RoomMutationEnvelopeStore<ScopeKey, Command : OutboxMutation>(
	private val pendingMutationDao: PendingMutationDao,
	private val transactionRunner: PersistenceTransactionRunner,
	private val storeId: String,
	private val scopeKeySerializer: KSerializer<ScopeKey>,
	private val commandSerializer: KSerializer<Command>,
	private val json: Json = Json
) : MutationEnvelopeStore<ScopeKey, Command> {
	override fun observePendingMutations(
		scopeKey: ScopeKey
	) = pendingMutationDao.observePendingMutations(
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
		return pendingMutationDao.getPendingMutations(
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
		return pendingMutationDao.getPendingMutation(
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
		transactionRunner.immediate {
			pendingMutationDao.deletePendingMutationsByReplaceKey(
				storeId = storeId,
				replaceKey = mutation.replaceKey
			)
			pendingMutationDao.upsertEntity(
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
		pendingMutationDao.upsertEntity(
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
		pendingMutationDao.deletePendingMutation(
			storeId = storeId,
			scopeKey = json.encodeToString(scopeKeySerializer, scopeKey),
			mutationId = mutationId
		)
	}
}
