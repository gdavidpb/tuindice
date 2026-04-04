package com.gdavidpb.tuindice.auth.data.repository

import com.gdavidpb.tuindice.auth.data.source.AuthDataSource
import com.gdavidpb.tuindice.auth.testing.DEFAULT_AUTH_ATTESTATION
import com.gdavidpb.tuindice.auth.testing.DEFAULT_BOOTSTRAP_TOKENS
import com.gdavidpb.tuindice.auth.testing.DEFAULT_REFRESH_TOKENS
import com.gdavidpb.tuindice.auth.testing.FakeAuthApiDataSource
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRepositoryContractTest {
	@Test
	fun bootstrapSignIn_returnsBootstrapToken_withoutPersistingSession() = runTest {
		val authDataSource = FakeAuthApiDataSource()
		val sessionRepository = FakeSessionRepository(usbId = "", accessToken = "", refreshToken = "")
		val reportingRepository = RecordingReportingRepository()
		val repository = AuthDataSource(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = reportingRepository
		)

		val bootstrapTokens = repository.bootstrapSignIn(
			usbId = "20261234",
			password = "secret123"
		)

		assertEquals(DEFAULT_BOOTSTRAP_TOKENS, bootstrapTokens)
		assertEquals("", sessionRepository.getUsbId())
		assertEquals("", sessionRepository.getAccessToken())
		assertEquals("", sessionRepository.getRefreshToken())
		assertEquals(null, reportingRepository.identifier)
		assertEquals(1, authDataSource.bootstrapCalls.size)
	}

	@Test
	fun exchangeSignIn_persistsTokens_andSetsIdentifier() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val authDataSource = FakeAuthApiDataSource()
		val repository = AuthDataSource(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		repository.exchangeSignIn(
			bootstrapAccessToken = "bootstrap-access-token",
			attestation = DEFAULT_AUTH_ATTESTATION
		)

		assertEquals("20261234", sessionRepository.getUsbId())
		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals(1, authDataSource.exchangeCalls.size)
		assertEquals("bootstrap-access-token", authDataSource.exchangeCalls.single().bootstrapAccessToken)
	}

	@Test
	fun reissueTokens_refreshesSessionTokens() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val authDataSource = FakeAuthApiDataSource()
		val repository = AuthDataSource(
			authApiDataSource = authDataSource,
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		repository.reissueTokens(
			usbId = "20261234",
			password = "new-secret",
			attestation = DEFAULT_AUTH_ATTESTATION
		)

		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals(1, authDataSource.reissueCalls.size)
		assertEquals("20261234", authDataSource.reissueCalls.single().usbId)
	}

	@Test
	fun refreshTokens_updatesSessionAndReturnsTokens() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val repository = AuthDataSource(
			authApiDataSource = FakeAuthApiDataSource(refreshTokens = DEFAULT_REFRESH_TOKENS),
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		val tokens = repository.refreshTokens(
			accessToken = "old-access",
			refreshToken = "old-refresh",
			attestation = DEFAULT_AUTH_ATTESTATION
		)

		assertEquals(DEFAULT_REFRESH_TOKENS, tokens)
		assertEquals(DEFAULT_REFRESH_TOKENS.accessToken, sessionRepository.getAccessToken())
		assertEquals(DEFAULT_REFRESH_TOKENS.refreshToken, sessionRepository.getRefreshToken())
	}

	@Test
	fun revokeTokens_delegatesToApiDataSource() = runTest {
		val authDataSource = FakeAuthApiDataSource()
		val repository = AuthDataSource(
			authApiDataSource = authDataSource,
			sessionRepository = FakeSessionRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		repository.revokeTokens(accessToken = "access-token")

		assertEquals(1, authDataSource.revokeCalls)
		assertEquals(listOf("access-token"), authDataSource.revokedAccessTokens)
	}
}
