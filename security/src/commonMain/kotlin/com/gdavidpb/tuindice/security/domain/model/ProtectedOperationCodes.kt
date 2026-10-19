package com.gdavidpb.tuindice.security.domain.model

object ProtectedOperationCodes {
	val AuthExchange = ProtectedOperationCode("auth.exchange")
	val AuthRefreshTokens = ProtectedOperationCode("auth.refresh_tokens")
	val AuthRevokeTokens = ProtectedOperationCode("auth.revoke_tokens")
	val AuthReissueTokens = ProtectedOperationCode("auth.reissue_tokens")
}
