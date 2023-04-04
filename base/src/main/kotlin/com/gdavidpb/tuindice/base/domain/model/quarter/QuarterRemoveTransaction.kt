package com.gdavidpb.tuindice.base.domain.model.quarter

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.google.gson.annotations.SerializedName

data class QuarterRemoveTransaction(
	@SerializedName("quarter_id") val quarterId: String
) : TransactionData