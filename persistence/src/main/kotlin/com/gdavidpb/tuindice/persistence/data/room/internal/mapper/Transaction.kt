package com.gdavidpb.tuindice.persistence.data.room.internal.mapper

import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.domain.model.Transaction

internal fun Transaction.toTransactionEntity() = TransactionEntity(
	id = id,
	reference = reference,
	type = type,
	action = action,
	status = status,
	timestamp = timestamp
)

internal fun TransactionEntity.toTransaction() = Transaction(
	id = id,
	reference = reference,
	type = type,
	action = action,
	status = status,
	timestamp = timestamp
)