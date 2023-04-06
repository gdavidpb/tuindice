package com.gdavidpb.tuindice.transactions.data.api.model.transaction

import com.google.gson.annotations.SerializedName

enum class TransactionRequestType {
	@SerializedName("quarter")
	QUARTER,
	@SerializedName("subject")
	SUBJECT,
	@SerializedName("evaluation")
	EVALUATION
}