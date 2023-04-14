package com.gdavidpb.tuindice.base.domain.model.transaction

data class Transaction(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val data: String
)