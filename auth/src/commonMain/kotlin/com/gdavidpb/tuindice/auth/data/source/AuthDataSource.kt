package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens

class AuthDataSource(
	private val authApiDataSource: AuthApiDataRepository,
	private val sessionRepository: SessionRepository,
	private val reportingRepository: ReportingRepository
) : AuthRepository {
	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	) {
		val tokens = authApiDataSource.issueTokens(
			usbId = usbId,
			password = password,
			attestedFlow = attestedFlow,
			attestation = attestation
		)

		sessionRepository.setAccessToken(tokens.accessToken)
		sessionRepository.setRefreshToken(tokens.refreshToken)

		if (attestedFlow == AttestedTokenFlow.IssueTokens) {
			sessionRepository.setUsbId(tokens.usbId)
			reportingRepository.setIdentifier(tokens.uid)
		}
	}

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		return authApiDataSource.refreshTokens(
			accessToken = accessToken,
			refreshToken = refreshToken,
			attestation = attestation
		).also { tokens ->
			sessionRepository.setAccessToken(tokens.accessToken)
			sessionRepository.setRefreshToken(tokens.refreshToken)
		}
	}

	override suspend fun revokeTokens() {
		authApiDataSource.revokeTokens()
	}
}
