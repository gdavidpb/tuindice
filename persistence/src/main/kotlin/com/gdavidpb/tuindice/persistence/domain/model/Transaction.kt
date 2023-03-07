package com.gdavidpb.tuindice.persistence.domain.model

data class Transaction<T : TransactionData>(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val status: TransactionStatus,
	val timestamp: Long,
	val data: T? = null,
	val id: String = "$action:$reference"
)