package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity

fun Transaction.toTransactionEntity(uid: String) = TransactionEntity(
	id = "$action:$reference",
	accountId = uid,
	reference = reference,
	type = type,
	action = action,
	timestamp = System.currentTimeMillis(),
	data = data
)

fun TransactionEntity.toTransaction() = Transaction(
	reference = reference,
	type = type,
	action = action,
	timestamp = timestamp,
	data = data
)