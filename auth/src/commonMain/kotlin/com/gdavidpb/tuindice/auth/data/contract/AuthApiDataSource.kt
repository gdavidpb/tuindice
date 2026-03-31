package com.gdavidpb.tuindice.auth.data.contract

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens

interface AuthApiDataSource {
	suspend fun issueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens

	suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens

	suspend fun revokeTokens()
}
