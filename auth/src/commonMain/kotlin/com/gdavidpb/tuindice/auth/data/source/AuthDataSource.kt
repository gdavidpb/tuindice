package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository

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
		val expectedSnapshot = sessionRepository.getActiveSessionSnapshot()
		val tokens = authApiDataSource.exchangeSignIn(
			bootstrapAccessToken = bootstrapAccessToken,
			attestation = attestation
		)

		if (expectedSnapshot != null) {
			persistIssuedTokensIfCurrent(
				tokens = tokens,
				expectedSnapshot = expectedSnapshot
			)
		} else {
			persistIssuedTokens(tokens)
		}
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) {
		val expectedSnapshot = sessionRepository.getActiveSessionSnapshot()
		val tokens = authApiDataSource.reissueTokens(
			usbId = usbId,
			password = password,
			attestedFlow = AttestedTokenFlow.ReissueTokens,
			attestation = attestation
		)

		if (expectedSnapshot != null) {
			persistIssuedTokensIfCurrent(
				tokens = tokens,
				expectedSnapshot = expectedSnapshot
			)
		} else {
			persistIssuedTokens(tokens)
		}
	}

	private suspend fun persistIssuedTokens(tokens: IssueTokens) {
		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = tokens.sessionId,
				accessToken = tokens.accessToken,
				refreshToken = tokens.refreshToken,
				usbId = tokens.usbId
			)
		)
		reportingRepository.setIdentifier(tokens.uid)
	}

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		val expectedSnapshot = sessionRepository.getActiveSessionSnapshot()

		return authApiDataSource.refreshTokens(
			sessionId = sessionId,
			refreshToken = refreshToken,
			attestation = attestation
		).also { tokens ->
			val refreshedSnapshot = SessionSnapshot(
				sessionId = tokens.sessionId,
				accessToken = tokens.accessToken,
				refreshToken = tokens.refreshToken,
				usbId = expectedSnapshot?.usbId ?: sessionRepository.getUsbId()
			)

			if (
				expectedSnapshot != null &&
				expectedSnapshot.sessionId == sessionId &&
				expectedSnapshot.refreshToken == refreshToken
			) {
				sessionRepository.replaceSessionSnapshotIfCurrent(
					expectedSnapshot = expectedSnapshot,
					newSnapshot = refreshedSnapshot
				)
			} else if (expectedSnapshot == null) {
				sessionRepository.setSessionSnapshot(refreshedSnapshot)
			}
		}
	}

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) {
		authApiDataSource.revokeTokens(
			sessionId = sessionId,
			refreshToken = refreshToken,
			attestation = attestation
		)
	}

	private suspend fun persistIssuedTokensIfCurrent(
		tokens: IssueTokens,
		expectedSnapshot: SessionSnapshot
	) {
		val didReplace = sessionRepository.replaceSessionSnapshotIfCurrent(
			expectedSnapshot = expectedSnapshot,
			newSnapshot = SessionSnapshot(
				sessionId = tokens.sessionId,
				accessToken = tokens.accessToken,
				refreshToken = tokens.refreshToken,
				usbId = tokens.usbId
			)
		)

		if (didReplace) {
			reportingRepository.setIdentifier(tokens.uid)
		}
	}
}
