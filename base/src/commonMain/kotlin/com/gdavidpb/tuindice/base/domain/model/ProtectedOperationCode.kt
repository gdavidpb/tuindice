package com.gdavidpb.tuindice.base.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class ProtectedOperationCode(
	val value: String
) {
	init {
		require(value.isNotBlank())
	}
}

object ProtectedOperationCodes {
	val AuthIssueTokens = ProtectedOperationCode("auth.issue_tokens")
	val AuthRefreshTokens = ProtectedOperationCode("auth.refresh_tokens")
	val AuthReissueTokens = ProtectedOperationCode("auth.reissue_tokens")
}
