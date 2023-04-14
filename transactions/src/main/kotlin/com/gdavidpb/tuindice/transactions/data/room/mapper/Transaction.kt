package com.gdavidpb.tuindice.transactions.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity

fun Transaction.toTransactionEntity(uid: String, ordinal: Long) = TransactionEntity(
	id = "$action:$reference",
	accountId = uid,
	reference = reference,
	type = type,
	action = action,
	ordinal = ordinal,
	data = data
)

fun TransactionEntity.toTransaction() = Transaction(
	reference = reference,
	type = type,
	action = action,
	data = data
)