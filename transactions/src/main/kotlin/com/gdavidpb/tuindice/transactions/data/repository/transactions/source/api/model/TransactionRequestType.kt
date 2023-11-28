package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionRequestType {
	@SerialName("quarter")
	QUARTER,

	@SerialName("subject")
	SUBJECT,

	@SerialName("evaluation")
	EVALUATION
}