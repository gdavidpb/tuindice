package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.login.domain.model.IssueTokens
import com.gdavidpb.tuindice.login.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens

interface LoginAuthApiDataSource {
	suspend fun issueTokens(
		usbId: String,
		password: String,
		flow: IssueTokensFlow,
		riskAttestation: RiskAttestation
	): IssueTokens

	suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		riskAttestation: RiskAttestation
	): RefreshTokens
}
