package com.gdavidpb.tuindice.auth.domain.repository

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.security.domain.model.Attestation

interface AuthRepository {
	suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens

	suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	)

	// The exchange of the recovery by credentials: it stores the tokens only while the stored session is still
	// [attemptedSnapshot], the one the recovery set out to replace. It never reads the session to decide.
	suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
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
