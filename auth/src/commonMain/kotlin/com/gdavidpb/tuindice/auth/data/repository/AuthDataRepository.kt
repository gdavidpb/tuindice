package com.gdavidpb.tuindice.auth.data.repository

import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository

class AuthDataRepository(
	private val authApiDataSource: AuthApiDataSource,
	private val sessionRepository: SessionRepository,
	private val reportingRepository: ReportingRepository
) : AuthRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		flow: IssueTokensFlow,
		riskAttestation: RiskAttestation
	) {
		val tokens = authApiDataSource.issueTokens(
			usbId = usbId,
			password = password,
			flow = flow,
			riskAttestation = riskAttestation
		)

		sessionRepository.setAccessToken(tokens.accessToken)
		sessionRepository.setRefreshToken(tokens.refreshToken)

		if (flow == IssueTokensFlow.IssueTokens) {
			sessionRepository.setUsbId(tokens.usbId)
			reportingRepository.setIdentifier(tokens.uid)
		}
	}

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		riskAttestation: RiskAttestation
	): RefreshTokens {
		return authApiDataSource.refreshTokens(
			accessToken = accessToken,
			refreshToken = refreshToken,
			riskAttestation = riskAttestation
		).also { tokens ->
			sessionRepository.setAccessToken(tokens.accessToken)
			sessionRepository.setRefreshToken(tokens.refreshToken)
		}
	}

	override suspend fun revokeTokens() {
		authApiDataSource.revokeTokens()
	}
}
