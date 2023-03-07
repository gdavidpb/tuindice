package com.gdavidpb.tuindice.persistence.data.api.response

import com.gdavidpb.tuindice.persistence.data.api.model.SyncAction
import com.gdavidpb.tuindice.persistence.data.api.model.SyncType
import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.google.gson.annotations.SerializedName

data class SyncResolutionResponse<T : TransactionData>(
	@SerializedName("reference") val reference: String,
	@SerializedName("type") val type: SyncType,
	@SerializedName("action") val action: SyncAction,
	@SerializedName("data") val data: T? = null
)