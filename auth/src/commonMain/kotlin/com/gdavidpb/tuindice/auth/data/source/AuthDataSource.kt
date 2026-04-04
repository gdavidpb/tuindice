package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens

class AuthDataSource(
	private val authApiDataSource: AuthApiDataRepository,
	private val sessionRepository: SessionRepository,
	private val reportingRepository: ReportingRepository
) : AuthRepository {
	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		return authApiDataSource.bootstrapSignIn(
			usbId = usbId,
			password = password
		)
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) {
		val tokens = authApiDataSource.exchangeSignIn(
			bootstrapAccessToken = bootstrapAccessToken,
			attestation = attestation
		)

		persistIssuedTokens(tokens)
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) {
		val tokens = authApiDataSource.reissueTokens(
			usbId = usbId,
			password = password,
			attestedFlow = AttestedTokenFlow.ReissueTokens,
			attestation = attestation
		)

		persistIssuedTokens(tokens)
	}

	private suspend fun persistIssuedTokens(tokens: IssueTokens) {
		sessionRepository.setAccessToken(tokens.accessToken)
		sessionRepository.setRefreshToken(tokens.refreshToken)
		sessionRepository.setUsbId(tokens.usbId)
		reportingRepository.setIdentifier(tokens.uid)
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

	override suspend fun revokeTokens(accessToken: String) {
		authApiDataSource.revokeTokens(accessToken = accessToken)
	}
}
