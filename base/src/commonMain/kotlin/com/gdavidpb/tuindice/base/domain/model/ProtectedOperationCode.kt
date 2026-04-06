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
	val AuthExchange = ProtectedOperationCode("auth.exchange")
	val AuthRefreshTokens = ProtectedOperationCode("auth.refresh_tokens")
	val AuthRevokeTokens = ProtectedOperationCode("auth.revoke_tokens")
	val AuthReissueTokens = ProtectedOperationCode("auth.reissue_tokens")
}
