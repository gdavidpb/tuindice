package com.gdavidpb.tuindice.persistence.data.room.internal.mapper

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionOperation
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity

fun Transaction<*>.toTransactionEntity() = TransactionEntity(
	id = "$action:$reference",
	accountId = uid,
	reference = reference,
	type = type,
	action = action,
	timestamp = timestamp
)

fun TransactionEntity.toTransaction() = Transaction.Builder<TransactionOperation>()
	.withReference(reference)
	.withTimestamp(timestamp)
	.withOperation(TODO())
	.build()