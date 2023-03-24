package com.gdavidpb.tuindice.persistence.data.api.response

import com.gdavidpb.tuindice.persistence.data.api.model.resolution.ResolutionResponseAction
import com.gdavidpb.tuindice.persistence.data.api.model.resolution.ResolutionResponseType
import com.google.gson.annotations.SerializedName

data class ResolutionResponse(
	@SerializedName("uid") val uid: String,
	@SerializedName("local_reference") val localReference: String,
	@SerializedName("remote_reference") val remoteReference: String,
	@SerializedName("type") val type: ResolutionResponseType,
	@SerializedName("action") val action: ResolutionResponseAction,
	@SerializedName("operation") val operation: ResolutionOperationResponse
)