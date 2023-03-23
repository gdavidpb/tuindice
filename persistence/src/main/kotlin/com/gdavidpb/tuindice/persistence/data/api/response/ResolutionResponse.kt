package com.gdavidpb.tuindice.persistence.data.api.response

import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestAction
import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestType
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.google.gson.annotations.SerializedName

data class ResolutionResponse(
	@SerializedName("uid") val uid: String,
	@SerializedName("local_reference") val localReference: String,
	@SerializedName("remote_reference") val remoteReference: String,
	@SerializedName("type") val type: TransactionRequestType,
	@SerializedName("action") val action: TransactionRequestAction,
	@SerializedName("data") val data: TransactionData? = null
)