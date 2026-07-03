package com.gdavidpb.tuindice.auth.data.repository

import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens

interface AuthApiDataRepository {
	suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens

	suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	): IssueTokens

	suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens

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
