package com.gdavidpb.tuindice.persistence.data.api.model.transaction

import com.google.gson.annotations.SerializedName

enum class TransactionRequestAction {
	@SerializedName("add")
	ADD,
	@SerializedName("update")
	UPDATE,
	@SerializedName("delete")
	DELETE
}