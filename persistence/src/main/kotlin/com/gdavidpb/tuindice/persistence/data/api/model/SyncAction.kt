package com.gdavidpb.tuindice.persistence.data.api.model

import com.google.gson.annotations.SerializedName

enum class SyncAction {
	@SerializedName("add")
	ADD,
	@SerializedName("update")
	UPDATE,
	@SerializedName("delete")
	DELETE
}