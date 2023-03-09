package com.gdavidpb.tuindice.persistence.domain.model

data class Resolution(
	val reference: String,
	val data: TransactionData? = null
)