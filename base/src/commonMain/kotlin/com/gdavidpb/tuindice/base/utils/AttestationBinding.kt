package com.gdavidpb.tuindice.base.utils

import com.gdavidpb.tuindice.base.domain.model.AttestedOperation

fun attestationBindingInput(
	sessionId: String,
	challenge: String,
	operation: AttestedOperation,
	requestHash: String
): String {
	return "$sessionId:$challenge:${operation.code}:$requestHash"
}
