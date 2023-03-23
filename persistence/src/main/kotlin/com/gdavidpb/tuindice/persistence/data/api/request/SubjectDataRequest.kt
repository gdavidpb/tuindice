package com.gdavidpb.tuindice.persistence.data.api.request

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.google.gson.annotations.SerializedName

data class SubjectDataRequest(
	@SerializedName("id") val id: String,
	@SerializedName("grade") val grade: Int
) : TransactionData