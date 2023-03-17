package com.gdavidpb.tuindice.persistence.data.api.request

import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.google.gson.annotations.SerializedName

data class SubjectDataRequest(
	@SerializedName("id") val id: String,
	@SerializedName("grade") val grade: Int
) : TransactionData