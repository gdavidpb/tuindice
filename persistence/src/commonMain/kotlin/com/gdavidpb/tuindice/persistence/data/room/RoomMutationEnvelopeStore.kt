package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.mapper.toMutationEnvelope
import com.gdavidpb.tuindice.persistence.data.room.mapper.toPendingMutationEntity
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class RoomMutationEnvelopeStore<Command : OutboxMutation>(
	private val pendingMutationDao: PendingMutationDao,
	private val transactionRunner: PersistenceTransactionRunner,
	private val storeId: String,
	private val commandSerializer: KSerializer<Command>,
	private val json: Json = Json
) : MutationEnvelopeStore<String, Command> {
	override fun observePendingMutations(
		scopeKey: String
	) = pendingMutationDao.observePendingMutations(
		storeId = storeId,
		scopeKey = scopeKey
	).map { entities ->
		entities.map { entity ->
			entity.toMutationEnvelope(
				commandSerializer = commandSerializer,
				json = json
			)
		}
	}

	override suspend fun getPendingMutations(
		scopeKey: String
	): List<MutationEnvelope<String, Command>> {
		return pendingMutationDao.getPendingMutations(
			storeId = storeId,
			scopeKey = scopeKey
		).map { entity ->
			entity.toMutationEnvelope(
				commandSerializer = commandSerializer,
				json = json
			)
		}
	}

	override fun observeMutations(
		scopeKey: String
	) = pendingMutationDao.observeMutations(
		storeId = storeId,
		scopeKey = scopeKey
	).map { entities ->
		entities.map { entity ->
			entity.toMutationEnvelope(
				commandSerializer = commandSerializer,
				json = json
			)
		}
	}

	override suspend fun getMutations(
		scopeKey: String
	): List<MutationEnvelope<String, Command>> {
		return pendingMutationDao.getMutations(
			storeId = storeId,
			scopeKey = scopeKey
		).map { entity ->
			entity.toMutationEnvelope(
				commandSerializer = commandSerializer,
				json = json
			)
		}
	}

	override suspend fun getPendingMutation(
		scopeKey: String,
		mutationId: String
	): MutationEnvelope<String, Command>? {
		return pendingMutationDao.getPendingMutation(
			storeId = storeId,
			scopeKey = scopeKey,
			mutationId = mutationId
		)?.toMutationEnvelope(
			commandSerializer = commandSerializer,
			json = json
		)
	}

	override suspend fun replacePendingMutation(
		mutation: MutationEnvelope<String, Command>
	) {
		transactionRunner.immediate {
			pendingMutationDao.deletePendingMutationsByReplaceKey(
				storeId = storeId,
				replaceKey = mutation.replaceKey
			)
			pendingMutationDao.upsertEntity(
				mutation.toPendingMutationEntity(
					storeId = storeId,
					commandSerializer = commandSerializer,
					json = json
				)
			)
		}
	}

	override suspend fun savePendingMutation(
		mutation: MutationEnvelope<String, Command>
	) {
		pendingMutationDao.upsertEntity(
			mutation.toPendingMutationEntity(
				storeId = storeId,
				commandSerializer = commandSerializer,
				json = json
			)
		)
	}

	override suspend fun deletePendingMutation(
		scopeKey: String,
		mutationId: String
	) {
		pendingMutationDao.deletePendingMutation(
			storeId = storeId,
			scopeKey = scopeKey,
			mutationId = mutationId
		)
	}

	override suspend fun requeueFailedMutations(
		scopeKey: String,
		retryableBefore: Long
	): Int {
		return pendingMutationDao.requeueMutations(
			storeId = storeId,
			scopeKey = scopeKey,
			requeueFrom = PendingMutationStatus.Failed.name,
			retryableBefore = retryableBefore,
			updatedAt = currentTimeMillis()
		)
	}
}
