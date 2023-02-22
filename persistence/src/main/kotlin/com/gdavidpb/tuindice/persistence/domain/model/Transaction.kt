package com.gdavidpb.tuindice.persistence.domain.model

data class Transaction(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val status: TransactionStatus,
	val timestamp: Long,
	val id: String = "$action:$reference"
)
