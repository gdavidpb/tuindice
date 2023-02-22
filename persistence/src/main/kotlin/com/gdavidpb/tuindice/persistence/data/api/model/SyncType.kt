package com.gdavidpb.tuindice.persistence.data.api.model

import com.google.gson.annotations.SerializedName

enum class SyncType {
	@SerializedName("quarter")
	QUARTER,
	@SerializedName("subject")
	SUBJECT,
	@SerializedName("evaluation")
	EVALUATION
}