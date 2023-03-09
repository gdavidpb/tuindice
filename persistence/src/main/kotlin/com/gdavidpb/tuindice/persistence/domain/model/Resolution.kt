package com.gdavidpb.tuindice.persistence.domain.model

data class Resolution(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val data: TransactionData? = null
)