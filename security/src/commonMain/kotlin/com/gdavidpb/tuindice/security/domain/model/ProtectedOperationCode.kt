package com.gdavidpb.tuindice.security.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class ProtectedOperationCode(
	val value: String
) {
	init {
		require(value.isNotBlank())
	}
}
