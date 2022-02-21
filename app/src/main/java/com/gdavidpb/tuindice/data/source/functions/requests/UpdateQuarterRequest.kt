package com.gdavidpb.tuindice.data.source.functions.requests

import com.google.gson.annotations.SerializedName

data class UpdateQuarterRequest(
	@SerializedName("qid") val quarterId: String,
	@SerializedName("subjects") val subjects: List<UpdateSubjectRequest>
)