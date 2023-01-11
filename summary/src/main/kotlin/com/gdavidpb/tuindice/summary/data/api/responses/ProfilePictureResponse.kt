package com.gdavidpb.tuindice.summary.data.api.responses

import com.google.gson.annotations.SerializedName

data class ProfilePictureResponse(
	@SerializedName("uid")
	val uid: String,
	@SerializedName("url")
	val url: String
)