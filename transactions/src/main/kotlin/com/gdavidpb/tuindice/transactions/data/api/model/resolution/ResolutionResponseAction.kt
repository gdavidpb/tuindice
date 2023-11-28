package com.gdavidpb.tuindice.transactions.data.api.model.resolution

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ResolutionResponseAction {
	@SerialName("add")
	ADD,

	@SerialName("update")
	UPDATE,

	@SerialName("delete")
	DELETE
}