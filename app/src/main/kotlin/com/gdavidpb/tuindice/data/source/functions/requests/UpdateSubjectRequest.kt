package com.gdavidpb.tuindice.data.source.functions.requests

import com.google.gson.annotations.SerializedName

data class UpdateSubjectRequest(
	@SerializedName("sid") val subjectId: String,
	@SerializedName("grade") val grade: Int
)