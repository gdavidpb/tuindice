package com.gdavidpb.tuindice.base.utils

import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCode

fun attestationBindingInput(
	sessionId: String,
	challenge: String,
	operationCode: ProtectedOperationCode,
	requestHash: String
): String {
	return "$sessionId:$challenge:${operationCode.value}:$requestHash"
}
