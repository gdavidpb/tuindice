package com.gdavidpb.tuindice.base.domain.model.transaction

data class Transaction(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val status: TransactionStatus,
	val timestamp: Long,
	val data: TransactionData? = null
)