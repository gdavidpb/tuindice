package com.gdavidpb.tuindice.persistence.data.api.response

import com.gdavidpb.tuindice.persistence.data.api.model.SyncAction
import com.gdavidpb.tuindice.persistence.data.api.model.SyncStatus
import com.gdavidpb.tuindice.persistence.data.api.model.SyncType
import com.google.gson.annotations.SerializedName

data class SyncResponse(
	@SerializedName("in_reference") val inReference: String,
	@SerializedName("out_reference") val outReference: String,
	@SerializedName("type") val type: SyncType,
	@SerializedName("action") val action: SyncAction,
	@SerializedName("status") val status: SyncStatus
)