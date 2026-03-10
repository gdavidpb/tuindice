package com.gdavidpb.tuindice.auth.data.repository

import com.gdavidpb.tuindice.auth.testing.DEFAULT_AUTH_ATTESTATION
import com.gdavidpb.tuindice.auth.testing.DEFAULT_REFRESH_TOKENS
import com.gdavidpb.tuindice.auth.testing.FakeAuthApiDataSource
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRepositoryContractTest {
	@Test
	fun signIn_persistsTokens_andSetsIdentifier() = runTest {
		val authDataSource = FakeAuthApiDataSource()
		val sessionRepository = FakeSessionRepository(usbId = "", accessToken = "", refreshToken = "")
		val reportingRepository = RecordingReportingRepository()
		val repository = AuthDataRepository(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = reportingRepository
		)

		repository.issueTokens(
			usbId = "20261234",
			password = "secret123",
			flow = IssueTokensFlow.IssueTokens,
			riskAttestation = DEFAULT_AUTH_ATTESTATION
		)

		assertEquals("20261234", sessionRepository.getUsbId())
		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals("uid-123", reportingRepository.identifier)
		assertEquals(1, authDataSource.issueCalls.size)
		assertEquals(IssueTokensFlow.IssueTokens, authDataSource.issueCalls.single().flow)
	}

	@Test
	fun updatePassword_refreshesSessionTokens() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val authDataSource = FakeAuthApiDataSource()
		val repository = AuthDataRepository(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		repository.issueTokens(
			usbId = "20261234",
			password = "new-secret",
			flow = IssueTokensFlow.ReissueTokens,
			riskAttestation = DEFAULT_AUTH_ATTESTATION
		)

		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals(IssueTokensFlow.ReissueTokens, authDataSource.issueCalls.single().flow)
	}

	@Test
	fun refreshTokens_updatesSessionAndReturnsTokens() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val repository = AuthDataRepository(
			authApiDataSource = FakeAuthApiDataSource(refreshTokens = DEFAULT_REFRESH_TOKENS),
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		val tokens = repository.refreshTokens(
			accessToken = "old-access",
			refreshToken = "old-refresh",
			riskAttestation = DEFAULT_AUTH_ATTESTATION
		)

		assertEquals(DEFAULT_REFRESH_TOKENS, tokens)
		assertEquals(DEFAULT_REFRESH_TOKENS.accessToken, sessionRepository.getAccessToken())
		assertEquals(DEFAULT_REFRESH_TOKENS.refreshToken, sessionRepository.getRefreshToken())
	}

	@Test
	fun revokeTokens_delegatesToApiDataSource() = runTest {
		val authDataSource = FakeAuthApiDataSource()
		val repository = AuthDataRepository(
			authApiDataSource = authDataSource,
			sessionRepository = FakeSessionRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		repository.revokeTokens()

		assertEquals(1, authDataSource.revokeCalls)
	}
}
