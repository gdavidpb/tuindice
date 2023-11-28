package com.gdavidpb.tuindice.transactions.data.api.model.transaction

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