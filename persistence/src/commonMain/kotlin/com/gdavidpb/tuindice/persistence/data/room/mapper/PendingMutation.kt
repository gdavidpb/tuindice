package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

fun <T : OutboxMutation> PendingMutationEntity.toPendingMutation(
	serializer: KSerializer<T>,
	json: Json
) = PendingMutation(
	mutationId = mutationId,
	mutation = json.decodeFromString(serializer, payload),
	expectedRevision = expectedRevision,
	status = PendingMutationStatus.valueOf(status),
	createdAt = createdAt,
	updatedAt = updatedAt,
	lastError = lastError
)

fun <T : OutboxMutation> PendingMutation<T>.toPendingMutationEntity(
	serializer: KSerializer<T>,
	json: Json
) = PendingMutationEntity(
	mutationId = mutationId,
	entityType = mutation.entityType,
	entityId = mutation.entityId,
	replaceKey = mutation.replaceKey,
	payload = json.encodeToString(serializer, mutation),
	expectedRevision = expectedRevision,
	status = status.name,
	createdAt = createdAt,
	updatedAt = updatedAt,
	lastError = lastError
)
