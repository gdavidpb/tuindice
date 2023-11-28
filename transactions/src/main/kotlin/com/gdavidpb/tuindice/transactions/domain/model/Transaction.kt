package com.gdavidpb.tuindice.transactions.domain.model

data class Transaction(
	val timestamp: Long = System.currentTimeMillis(),
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val data: String? = null
)