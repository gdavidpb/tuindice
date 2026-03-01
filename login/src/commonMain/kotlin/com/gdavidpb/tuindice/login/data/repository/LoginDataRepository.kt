package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.model.RefreshTokens
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository

class LoginDataRepository(
	private val authApiDataSource: LoginAuthApiDataSource,
	private val messagingApiDataSource: LoginMessagingApiDataSource,
	private val messagingDataSource: LoginMessagingDataSource,
	private val sessionRepository: SessionRepository,
	private val reportingRepository: ReportingRepository
) : LoginRepository {
	override suspend fun signIn(
		usbId: String,
		password: String,
		attestation: Attestation
	) {
		val tokens = authApiDataSource.issueTokens(
			usbId = usbId,
			password = password,
			attestation = attestation
		)

		sessionRepository.setUsbId(tokens.usbId)
		sessionRepository.setAccessToken(tokens.accessToken)
		sessionRepository.setRefreshToken(tokens.refreshToken)
		reportingRepository.setIdentifier(tokens.uid)

		val messagingToken = messagingDataSource.getToken()
			.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable.")

		messagingApiDataSource.subscribe(messagingToken)
	}

	override suspend fun updatePassword(
		usbId: String,
		password: String,
		attestation: Attestation
	) {
		val tokens = authApiDataSource.issueTokens(
			usbId = usbId,
			password = password,
			attestation = attestation
		)

		sessionRepository.setAccessToken(tokens.accessToken)
		sessionRepository.setRefreshToken(tokens.refreshToken)
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
}
