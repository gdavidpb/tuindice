package com.gdavidpb.tuindice.persistence.data.api.model.resolution

import com.google.gson.annotations.SerializedName

enum class ResolutionResponseAction {
	@SerializedName("add")
	ADD,
	@SerializedName("update")
	UPDATE,
	@SerializedName("delete")
	DELETE
}