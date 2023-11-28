package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionRequestAction {
	@SerialName("add")
	ADD,

	@SerialName("update")
	UPDATE,

	@SerialName("delete")
	DELETE
}