package com.gdavidpb.tuindice.persistence.data.room.internal.mapper

import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionData

internal fun Transaction<TransactionData>.toTransactionEntity() = TransactionEntity(
	reference = reference,
	type = type,
	action = action,
	status = status,
	timestamp = timestamp,
	id = id
)

internal fun TransactionEntity.toTransaction() = Transaction<TransactionData>(
	reference = reference,
	type = type,
	action = action,
	status = status,
	timestamp = timestamp,
	id = id
)