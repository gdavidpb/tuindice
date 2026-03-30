package com.gdavidpb.tuindice.base.utils

fun attestationBindingInput(
	sessionId: String,
	challenge: String,
	bindingCode: String,
	requestHash: String
): String {
	return "$sessionId:$challenge:$bindingCode:$requestHash"
}
