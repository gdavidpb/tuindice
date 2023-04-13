package com.gdavidpb.tuindice.transactions.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity

fun Transaction.toTransactionEntity(uid: String) = TransactionEntity(
	id = "$action:$reference",
	accountId = uid,
	reference = reference,
	type = type,
	action = action,
	timestamp = timestamp,
	data = data
)

fun TransactionEntity.toTransaction() = Transaction.Builder()
	.withReference(reference)
	.withTimestamp(timestamp)
	.withData(data)
	.build()