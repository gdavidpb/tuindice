package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

fun <Command : OutboxMutation> PendingMutationEntity.toMutationEnvelope(
	commandSerializer: KSerializer<Command>,
	json: Json
) = MutationEnvelope<String, Command>(
	mutationId = mutationId,
	scopeKey = scopeKey,
	command = json.decodeFromString(commandSerializer, payload),
	precondition = when (preconditionType) {
		"none" -> MutationPrecondition.None
		else -> MutationPrecondition.Revision(expectedRevision)
	},
	status = PendingMutationStatus.valueOf(status),
	createdAt = createdAt,
	updatedAt = updatedAt,
	lastError = lastError,
	replaceKey = replaceKey
)

fun <Command : OutboxMutation> MutationEnvelope<String, Command>.toPendingMutationEntity(
	storeId: String,
	commandSerializer: KSerializer<Command>,
	json: Json
) = PendingMutationEntity(
	mutationId = mutationId,
	storeId = storeId,
	scopeKey = scopeKey,
	entityType = entityType,
	entityId = entityId,
	replaceKey = replaceKey,
	payload = json.encodeToString(commandSerializer, command),
	preconditionType = when (precondition) {
		MutationPrecondition.None -> "none"
		is MutationPrecondition.Revision -> "revision"
	},
	expectedRevision = (precondition as? MutationPrecondition.Revision)?.value ?: 0L,
	status = status.name,
	createdAt = createdAt,
	updatedAt = updatedAt,
	lastError = lastError
)
