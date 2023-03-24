package com.gdavidpb.tuindice.persistence.data.api.model.resolution

import com.google.gson.annotations.SerializedName

enum class ResolutionResponseType {
	@SerializedName("quarter")
	QUARTER,
	@SerializedName("subject")
	SUBJECT,
	@SerializedName("evaluation")
	EVALUATION
}