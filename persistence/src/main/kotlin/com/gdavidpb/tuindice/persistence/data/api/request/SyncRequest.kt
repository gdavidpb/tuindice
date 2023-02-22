package com.gdavidpb.tuindice.persistence.data.api.request

import com.gdavidpb.tuindice.persistence.data.api.model.SyncAction
import com.gdavidpb.tuindice.persistence.data.api.model.SyncType
import com.google.gson.annotations.SerializedName

data class SyncRequest<T>(
	@SerializedName("reference") val reference: String,
	@SerializedName("type") val type: SyncType,
	@SerializedName("action") val action: SyncAction,
	@SerializedName("timestamp") val timestamp: Long,
	@SerializedName("data") val data: T
)