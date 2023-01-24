package com.gdavidpb.tuindice.record.data.api.request

import com.google.gson.annotations.SerializedName

data class UpdateSubjectRequest(
	@SerializedName("subject_id") val subjectId: String,
	@SerializedName("grade") val grade: Int
)