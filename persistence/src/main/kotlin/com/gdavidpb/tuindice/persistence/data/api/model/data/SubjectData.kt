package com.gdavidpb.tuindice.persistence.data.api.model.data

import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.google.gson.annotations.SerializedName

data class SubjectData(
	@SerializedName("id") val id: String,
	@SerializedName("grade") val grade: Int
) : TransactionData