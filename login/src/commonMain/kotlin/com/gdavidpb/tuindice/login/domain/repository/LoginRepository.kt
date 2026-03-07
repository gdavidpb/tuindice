package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.login.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens

interface LoginRepository {
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
}
