package com.gdavidpb.tuindice.persistence.data.api.request.subject

import com.gdavidpb.tuindice.persistence.data.api.request.TransactionOperationRequest
import com.google.gson.annotations.SerializedName

data class SubjectUpdateRequest(
	@SerializedName("id") val id: String,
	@SerializedName("grade") val grade: Int
) : TransactionOperationRequest