package com.gdavidpb.tuindice.persistence.data.api.model

import com.google.gson.annotations.SerializedName

enum class SyncStatus {
	@SerializedName("accepted")
	ACCEPTED,
	@SerializedName("rejected")
	REJECTED
}