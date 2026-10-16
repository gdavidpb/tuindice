package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.data.source.session.jwtWithPayload
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class KtorClientProactiveRefreshTest {
	@Test
	fun proactiveRefresh_replacesExpiredTokenBeforeSending() = runTest {
		val sessionRepository = expiredSessionRepository()
		val authRepository = ProactiveRefreshAuthRepository(sessionRepository = sessionRepository)
		val attestationRepository = CountingAttestationRepository()
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = authRepository,
			attestationRepository = attestationRepository
		)

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(listOf("Bearer access-new"), client.authorizationHeaders())
		assertEquals(1, authRepository.refreshCalls)
		assertEquals(1, attestationRepository.requests.size)
	}

	@Test
	fun proactiveRefresh_sharesOneRefreshAcrossConcurrentRequests() = runTest {
		val sessionRepository = expiredSessionRepository()
		val authRepository = ProactiveRefreshAuthRepository(sessionRepository = sessionRepository)
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = authRepository
		)

		try {
			val first = async { client.get("https://api.tuindice.app/record/v5/sync") }
			val second = async { client.get("https://api.tuindice.app/evaluations/v3") }

			assertEquals(HttpStatusCode.OK, first.await().status)
			assertEquals(HttpStatusCode.OK, second.await().status)
		} finally {
			client.close()
		}

		assertEquals(1, authRepository.refreshCalls)
		assertEquals(listOf("Bearer access-new", "Bearer access-new"), client.authorizationHeaders())
	}

	@Test
	fun proactiveRefresh_requestStartedMidRefreshWaitsForFreshToken() = runTest {
		val sessionRepository = expiredSessionRepository()
		val refreshStarted = CompletableDeferred<Unit>()
		val proceedRefresh = CompletableDeferred<Unit>()
		val authRepository = ProactiveRefreshAuthRepository(
			sessionRepository = sessionRepository,
			beforeRefresh = {
				refreshStarted.complete(Unit)
				proceedRefresh.await()
			}
		)
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = authRepository
		)

		try {
			val first = async { client.get("https://api.tuindice.app/record/v5/sync") }

			refreshStarted.await()

			val second = async { client.get("https://api.tuindice.app/users/v1") }

			testScheduler.advanceUntilIdle()
			proceedRefresh.complete(Unit)

			assertEquals(HttpStatusCode.OK, first.await().status)
			assertEquals(HttpStatusCode.OK, second.await().status)
		} finally {
			client.close()
		}

		assertEquals(1, authRepository.refreshCalls)
		assertEquals(listOf("Bearer access-new", "Bearer access-new"), client.authorizationHeaders())
	}

	@Test
	fun proactiveRefresh_sendsOpaqueTokensUntouched() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "bootstrap.mock.access.token",
			refreshToken = "refresh-old"
		)
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = UnusedAuthRepository
		)

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(listOf("Bearer bootstrap.mock.access.token"), client.authorizationHeaders())
	}

	@Test
	fun proactiveRefresh_sendsFreshJwtUntouched() = runTest {
		val freshToken = jwtWithPayload("""{"exp":${nowEpochSeconds() + 3600}}""")
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = freshToken,
			refreshToken = "refresh-old"
		)
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = UnusedAuthRepository
		)

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(listOf("Bearer $freshToken"), client.authorizationHeaders())
	}

	@Test
	fun proactiveRefresh_fallsBackToStoredTokenOnTransportFailure() = runTest {
		val expiredToken = expiredJwt()
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = expiredToken,
			refreshToken = "refresh-old"
		)
		val authRepository = UnavailableRefreshAuthRepository()
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = authRepository
		)

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(listOf("Bearer $expiredToken"), client.authorizationHeaders())
		assertEquals(1, authRepository.refreshCalls)
		assertFalse(sessionRepository.cleared)
	}

	@Test
	fun proactiveRefresh_retriesOnNextRequestAfterATransientFailure() = runTest {
		val expiredToken = expiredJwt()
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = expiredToken,
			refreshToken = "refresh-old"
		)
		val authRepository = FailOnceThenRefreshAuthRepository(sessionRepository = sessionRepository)
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = authRepository
		)

		try {
			client.get("https://api.tuindice.app/record/v5/sync")
			client.get("https://api.tuindice.app/users/v1")
		} finally {
			client.close()
		}

		// A transient transport failure must not latch: the second request has to
		// attempt its own refresh instead of resending the known-expired token.
		assertEquals(2, authRepository.refreshCalls)
		assertEquals(
			listOf("Bearer $expiredToken", "Bearer access-new"),
			client.authorizationHeaders()
		)
	}

	@Test
	fun proactiveRefresh_arbitratesPersistentlyExpiringTokenOnce() = runTest {
		val stillExpiredToken = jwtWithPayload("""{"exp":2000}""")
		val sessionRepository = expiredSessionRepository()
		val authRepository = ProactiveRefreshAuthRepository(
			sessionRepository = sessionRepository,
			newAccessToken = stillExpiredToken
		)
		val client = proactiveClient(
			sessionRepository = sessionRepository,
			authRepository = authRepository
		)

		try {
			client.get("https://api.tuindice.app/record/v5/sync")
			client.get("https://api.tuindice.app/users/v1")
		} finally {
			client.close()
		}

		assertEquals(1, authRepository.refreshCalls)
		assertEquals(
			listOf("Bearer $stillExpiredToken", "Bearer $stillExpiredToken"),
			client.authorizationHeaders()
		)
	}
}

private fun nowEpochSeconds(): Long = currentTimeMillis() / 1000L

private fun expiredJwt(): String = jwtWithPayload("""{"exp":1000}""")

private fun expiredSessionRepository(): FakeSessionRepository {
	return FakeSessionRepository(
		sessionId = "session-old",
		usbId = "12-34567",
		accessToken = expiredJwt(),
		refreshToken = "refresh-old"
	)
}

private fun proactiveClient(
	sessionRepository: SessionRepository,
	authRepository: AuthRepository,
	attestationRepository: AttestationRepository = CountingAttestationRepository()
): HttpClient {
	val sessionRecoveryRepository = sessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		attestationRepository = attestationRepository,
		authRepository = authRepository
	)

	return HttpClient(
		// The handler runs on the engine's dispatcher with no exclusion: it records nothing itself
		// (a list written from there races with the test reading it) and the engine's own history
		// is read once the requests are done.
		MockEngine { respondOk() }
	) {
		install(Auth) {
			installSharedBearerAuth(
				sessionRepository = sessionRepository,
				sessionRecoveryRepository = sessionRecoveryRepository
			)
		}
		installCurrentSessionBearerAuth(sessionRecoveryRepository)
	}
}

private fun sessionRecoveryDataSource(
	sessionRepository: SessionRepository,
	attestationRepository: AttestationRepository,
	authRepository: AuthRepository,
	credentialsRepository: CredentialsRepository = FakeCredentialsRepository(),
	sessionCoroutineScope: SessionCoroutineScope = testSessionCoroutineScope()
): SessionRecoveryDataSource {
	return SessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		applicationRepository = RecordingApplicationRepository(),
		sessionInvalidationRepository = FakeSessionInvalidationRepository(),
		syncStatusRepository = FakeSyncStatusRepository(),
		attestationRepository = attestationRepository,
		authRepository = authRepository,
		credentialsRepository = credentialsRepository,
		settingsRepository = FakeSettingsRepository(),
		reportingRepository = RecordingReportingRepository(),
		sessionCoroutineScope = sessionCoroutineScope,
		appCoroutineScope = testAppCoroutineScope()
	)
}

private class CountingAttestationRepository : AttestationRepository {
	val requests = mutableListOf<AttestationRequest>()

	override suspend fun attest(request: AttestationRequest): Attestation {
		requests += request
		return Attestation(token = "attestation-token")
	}
}

private class ProactiveRefreshAuthRepository(
	private val sessionRepository: SessionRepository,
	private val newAccessToken: String = "access-new",
	private val beforeRefresh: suspend () -> Unit = {}
) : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	var refreshCalls = 0

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		beforeRefresh()
		refreshCalls++
		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "session-new",
				accessToken = newAccessToken,
				refreshToken = "refresh-new",
				usbId = "12-34567"
			)
		)
		return RefreshTokens(
			sessionId = "session-new",
			accessToken = newAccessToken,
			refreshToken = "refresh-new",
			expiresIn = 3600
		)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private class FailOnceThenRefreshAuthRepository(
	private val sessionRepository: SessionRepository
) : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	var refreshCalls = 0

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		refreshCalls++

		if (refreshCalls == 1) throw clientRequestException(HttpStatusCode.ServiceUnavailable)

		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "session-new",
				accessToken = "access-new",
				refreshToken = "refresh-new",
				usbId = "12-34567"
			)
		)
		return RefreshTokens(
			sessionId = "session-new",
			accessToken = "access-new",
			refreshToken = "refresh-new",
			expiresIn = 3600
		)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private class UnavailableRefreshAuthRepository : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	var refreshCalls = 0

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		refreshCalls++
		throw clientRequestException(HttpStatusCode.ServiceUnavailable)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private object UnusedAuthRepository : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = error("unused in this test")

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = error("unused in this test")

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused in this test")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens = error("unused in this test")

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused in this test")
}
