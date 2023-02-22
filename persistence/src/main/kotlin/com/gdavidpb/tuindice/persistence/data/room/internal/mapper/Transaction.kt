package com.gdavidpb.tuindice.persistence.data.room.internal.mapper

import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.domain.model.Transaction

internal fun Transaction.toTransactionEntity() = TransactionEntity(
	reference = reference,
	type = type,
	action = action,
	status = status,
	timestamp = timestamp,
	id = id
)

internal fun TransactionEntity.toTransaction() = Transaction(
	reference = reference,
	type = type,
	action = action,
	status = status,
	timestamp = timestamp,
	id = id
)