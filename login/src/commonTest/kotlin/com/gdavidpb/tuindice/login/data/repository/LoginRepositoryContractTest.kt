package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.testing.DEFAULT_LOGIN_ATTESTATION
import com.gdavidpb.tuindice.login.testing.DEFAULT_REFRESH_TOKENS
import com.gdavidpb.tuindice.login.testing.FakeLoginAuthApiDataSource
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRepositoryContractTest {
	@Test
	fun signIn_persistsTokens_andSetsIdentifier() = runTest {
		val authDataSource = FakeLoginAuthApiDataSource()
		val sessionRepository = FakeSessionRepository(usbId = "", accessToken = "", refreshToken = "")
		val reportingRepository = RecordingReportingRepository()
		val repository = LoginDataRepository(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = reportingRepository
		)

		repository.issueTokens(
			usbId = "20261234",
			password = "secret123",
			flow = IssueTokensFlow.IssueTokens,
			riskAttestation = DEFAULT_LOGIN_ATTESTATION
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
		val authDataSource = FakeLoginAuthApiDataSource()
		val repository = LoginDataRepository(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		repository.issueTokens(
			usbId = "20261234",
			password = "new-secret",
			flow = IssueTokensFlow.ReissueTokens,
			riskAttestation = DEFAULT_LOGIN_ATTESTATION
		)

		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals(IssueTokensFlow.ReissueTokens, authDataSource.issueCalls.single().flow)
	}

	@Test
	fun refreshTokens_updatesSessionAndReturnsTokens() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val repository = LoginDataRepository(
			authApiDataSource = FakeLoginAuthApiDataSource(refreshTokens = DEFAULT_REFRESH_TOKENS),
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		val tokens = repository.refreshTokens(
			accessToken = "old-access",
			refreshToken = "old-refresh",
			riskAttestation = DEFAULT_LOGIN_ATTESTATION
		)

		assertEquals(DEFAULT_REFRESH_TOKENS, tokens)
		assertEquals(DEFAULT_REFRESH_TOKENS.accessToken, sessionRepository.getAccessToken())
		assertEquals(DEFAULT_REFRESH_TOKENS.refreshToken, sessionRepository.getRefreshToken())
	}
}
