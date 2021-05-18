package com.gdavidpb.tuindice.data.source.functions.requests

import com.google.gson.annotations.SerializedName

data class SignInRequest(
	@SerializedName("usb_id") val usbId: String,
	@SerializedName("password") val password: String
)
