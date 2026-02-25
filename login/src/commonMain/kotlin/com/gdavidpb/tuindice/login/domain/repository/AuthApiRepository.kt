package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens

interface AuthApiRepository {
	suspend fun issueTokens(usbId: String, password: String, attestation: Attestation): IssueTokens
	suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens
	suspend fun revokeTokens()
}