package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.testing.DEFAULT_LOGIN_ATTESTATION
import com.gdavidpb.tuindice.login.testing.DEFAULT_REFRESH_TOKENS
import com.gdavidpb.tuindice.login.testing.FakeLoginAuthApiDataSource
import com.gdavidpb.tuindice.login.testing.FakeLoginMessagingDataSource
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingLoginMessagingApiDataSource
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRepositoryContractTest {
	@Test
	fun signIn_persistsTokens_setsIdentifier_andSubscribesMessaging() = runTest {
		val authDataSource = FakeLoginAuthApiDataSource()
		val messagingApiDataSource = RecordingLoginMessagingApiDataSource()
		val sessionRepository = FakeSessionRepository(usbId = "", accessToken = "", refreshToken = "")
		val reportingRepository = RecordingReportingRepository()
		val repository = LoginDataRepository(
			authApiDataSource = authDataSource,
			messagingApiDataSource = messagingApiDataSource,
			messagingDataSource = FakeLoginMessagingDataSource(token = "push-token"),
			sessionRepository = sessionRepository,
			reportingRepository = reportingRepository
		)

		repository.signIn(
			usbId = "20261234",
			password = "secret123",
			attestation = DEFAULT_LOGIN_ATTESTATION
		)

		assertEquals("20261234", sessionRepository.getUsbId())
		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals("uid-123", reportingRepository.identifier)
		assertEquals("push-token", messagingApiDataSource.subscribedToken)
		assertEquals(1, authDataSource.issueCalls.size)
	}

	@Test
	fun updatePassword_refreshesSessionTokens_withoutSubscribingMessaging() = runTest {
		val messagingApiDataSource = RecordingLoginMessagingApiDataSource()
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val repository = LoginDataRepository(
			authApiDataSource = FakeLoginAuthApiDataSource(),
			messagingApiDataSource = messagingApiDataSource,
			messagingDataSource = FakeLoginMessagingDataSource(token = "push-token"),
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		repository.updatePassword(
			usbId = "20261234",
			password = "new-secret",
			attestation = DEFAULT_LOGIN_ATTESTATION
		)

		assertEquals("access-token", sessionRepository.getAccessToken())
		assertEquals("refresh-token", sessionRepository.getRefreshToken())
		assertEquals(null, messagingApiDataSource.subscribedToken)
	}

	@Test
	fun refreshTokens_updatesSessionAndReturnsTokens() = runTest {
		val sessionRepository = FakeSessionRepository(accessToken = "old-access", refreshToken = "old-refresh")
		val repository = LoginDataRepository(
			authApiDataSource = FakeLoginAuthApiDataSource(refreshTokens = DEFAULT_REFRESH_TOKENS),
			messagingApiDataSource = RecordingLoginMessagingApiDataSource(),
			messagingDataSource = FakeLoginMessagingDataSource(),
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		val tokens = repository.refreshTokens(
			accessToken = "old-access",
			refreshToken = "old-refresh",
			attestation = DEFAULT_LOGIN_ATTESTATION
		)

		assertEquals(DEFAULT_REFRESH_TOKENS, tokens)
		assertEquals(DEFAULT_REFRESH_TOKENS.accessToken, sessionRepository.getAccessToken())
		assertEquals(DEFAULT_REFRESH_TOKENS.refreshToken, sessionRepository.getRefreshToken())
	}
}
