package com.gdavidpb.tuindice.auth.domain.repository

import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens

interface AuthRepository {
	suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens

	suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	)

	suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	)

	suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens

	suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	)
}
