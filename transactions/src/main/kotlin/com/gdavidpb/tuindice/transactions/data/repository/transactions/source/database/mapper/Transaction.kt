package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.mapper

import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionType

fun Transaction.toTransactionEntity(uid: String) = TransactionEntity(
	id = "$action:$reference",
	accountId = uid,
	reference = reference,
	type = type.ordinal,
	action = action.ordinal,
	timestamp = System.currentTimeMillis(),
	data = data
)

fun TransactionEntity.toTransaction() = Transaction(
	reference = reference,
	type = TransactionType.entries[type],
	action = TransactionAction.entries[action],
	timestamp = timestamp,
	data = data
)