package com.gdavidpb.tuindice.base.domain.model.subject

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.google.gson.annotations.SerializedName

data class SubjectUpdateTransaction(
	@SerializedName("subject_id") val subjectId: String,
	@SerializedName("grade") val grade: Int
) : TransactionData