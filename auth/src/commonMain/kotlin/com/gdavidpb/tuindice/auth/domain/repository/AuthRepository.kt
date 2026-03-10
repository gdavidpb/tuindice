package com.gdavidpb.tuindice.auth.domain.repository

import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens

interface AuthRepository {
	suspend fun issueTokens(
		usbId: String,
		password: String,
		flow: IssueTokensFlow,
		riskAttestation: RiskAttestation
	)

	suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		riskAttestation: RiskAttestation
	): RefreshTokens

	suspend fun revokeTokens()
}
