package com.gdavidpb.tuindice.persistence.domain.model

data class Resolution(
	val uid: String,
	val localReference: String,
	val remoteReference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val data: TransactionData? = null
)