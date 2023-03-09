package com.gdavidpb.tuindice.persistence.domain.model

data class Transaction(
	val id: String,
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val status: TransactionStatus,
	val timestamp: Long,
	val data: TransactionData? = null
)