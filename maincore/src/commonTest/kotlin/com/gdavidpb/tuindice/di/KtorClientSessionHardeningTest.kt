package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.extension.isAccessRejected
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KtorClientSessionHardeningTest {
	@Test
	fun recover_preservesSessionWhenAttestationIsRejectedWithoutAuthErrorHeader() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val dataSource = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			attestationRepository = RejectedAttestationRepository(
				throwable = clientRequestException(HttpStatusCode.Forbidden)
			),
			credentialsRepository = FakeCredentialsRepository(password = "secret")
		)

		val thrown = runCatching {
			dataSource.recoverUnauthorizedSession(
				attemptedAuthorizationAccessToken = "access-old",
				attemptedCachedAccessToken = "access-old",
				attemptedCachedRefreshToken = "refresh-old"
			)
		}.exceptionOrNull()

		assertNotNull(thrown)
		assertFalse(thrown.isAccessRejected())
		assertFalse(sessionRepository.cleared)
		assertEquals(0, sessionInvalidationRepository.invalidationCalls)
	}

	@Test
	fun recover_bootstrapExchangesWhenAttestationReportsSessionSuperseded() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val credentialsRepository = FakeCredentialsRepository(password = "secret")
		val authRepository = BootstrapExchangePersistingAuthRepository(
			sessionRepository = sessionRepository
		)
		val dataSource = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			attestationRepository = SupersededRefreshAttestationRepository(),
			authRepository = authRepository,
			credentialsRepository = credentialsRepository
		)

		val snapshot = dataSource.recoverUnauthorizedSession(
			attemptedAuthorizationAccessToken = "access-old",
			attemptedCachedAccessToken = "access-old",
			attemptedCachedRefreshToken = "refresh-old"
		)

		assertEquals("access-exchanged", snapshot?.accessToken)
		assertEquals(1, authRepository.bootstrapCalls)
		assertEquals(1, authRepository.exchangeCalls)
	}

	@Test
	fun insufficientScope_refreshesAndRetriesStaleTokenWithoutInvalidation() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val sessionRecoveryRepository = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			attestationRepository = TokenAttestationRepository,
			authRepository = RefreshingAuthRepository(sessionRepository = sessionRepository)
		)
		val authorizationHeaders = mutableListOf<String>()
		val client = HttpClient(
			MockEngine { request ->
				val authorization = checkNotNull(request.headers[HttpHeaders.Authorization])
				authorizationHeaders += authorization

				if (authorization == "Bearer access-old") {
					respond(
						content = "",
						status = HttpStatusCode.Forbidden,
						headers = headersOf(AuthErrorHeaders.HEADER, AuthErrorHeaders.INSUFFICIENT_SCOPE)
					)
				} else {
					respondOk()
				}
			}
		) {
			expectSuccess = true

			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					sessionRecoveryRepository = sessionRecoveryRepository
				)
			}
			installInsufficientScopeSessionInvalidation(
				sessionRepository = sessionRepository,
				sessionRecoveryRepository = sessionRecoveryRepository
			)
		}

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(
			listOf("Bearer access-old", "Bearer access-new"),
			authorizationHeaders
		)
		assertFalse(sessionRepository.cleared)
		assertEquals(0, sessionInvalidationRepository.invalidationCalls)
	}

	@Test
	fun insufficientScope_invalidatesSessionWhenCurrentTokenStillLacksScope() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val sessionRecoveryRepository = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			attestationRepository = TokenAttestationRepository,
			authRepository = RefreshingAuthRepository(sessionRepository = sessionRepository)
		)
		val client = HttpClient(
			MockEngine {
				respond(
					content = "",
					status = HttpStatusCode.Forbidden,
					headers = headersOf(AuthErrorHeaders.HEADER, AuthErrorHeaders.INSUFFICIENT_SCOPE)
				)
			}
		) {
			expectSuccess = true

			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					sessionRecoveryRepository = sessionRecoveryRepository
				)
			}
			installInsufficientScopeSessionInvalidation(
				sessionRepository = sessionRepository,
				sessionRecoveryRepository = sessionRecoveryRepository
			)
		}

		val thrown = try {
			runCatching { client.get("https://api.tuindice.app/record/v5/sync") }.exceptionOrNull()
		} finally {
			client.close()
		}

		assertNotNull(thrown)
		assertTrue(sessionRepository.cleared)
		assertEquals(1, sessionInvalidationRepository.invalidationCalls)
	}
}

private fun sessionRecoveryDataSource(
	sessionRepository: SessionRepository,
	attestationRepository: AttestationRepository,
	sessionInvalidationRepository: FakeSessionInvalidationRepository = FakeSessionInvalidationRepository(),
	authRepository: AuthRepository = RejectedAuthRepository,
	credentialsRepository: CredentialsRepository = FakeCredentialsRepository()
): SessionRecoveryDataSource {
	return SessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		applicationRepository = RecordingApplicationRepository(),
		sessionInvalidationRepository = sessionInvalidationRepository,
		syncStatusRepository = FakeSyncStatusRepository(),
		attestationRepository = attestationRepository,
		authRepository = authRepository,
		credentialsRepository = credentialsRepository,
		sessionCoroutineScope = testSessionCoroutineScope()
	)
}

private object TokenAttestationRepository : AttestationRepository {
	override suspend fun attest(request: AttestationRequest): Attestation {
		return Attestation(token = "attestation-token")
	}
}

private class RejectedAttestationRepository(
	private val throwable: Throwable
) : AttestationRepository {
	override suspend fun attest(request: AttestationRequest): Attestation = throw throwable
}

private class SupersededRefreshAttestationRepository : AttestationRepository {
	override suspend fun attest(request: AttestationRequest): Attestation {
		if (request.operationCode == ProtectedOperationCodes.AuthRefreshTokens) {
			throw clientRequestException(
				statusCode = HttpStatusCode.Unauthorized,
				headers = mapOf(AuthErrorHeaders.HEADER to AuthErrorHeaders.SESSION_SUPERSEDED)
			)
		}

		return Attestation(token = "attestation-token")
	}
}

private object RejectedAuthRepository : AuthRepository {
	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = throw clientRequestException(HttpStatusCode.Unauthorized)

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = throw clientRequestException(HttpStatusCode.Unauthorized)

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = throw clientRequestException(HttpStatusCode.Unauthorized)

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens = throw clientRequestException(HttpStatusCode.Unauthorized)

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = throw clientRequestException(HttpStatusCode.Unauthorized)
}

private class RefreshingAuthRepository(
	private val sessionRepository: SessionRepository
) : AuthRepository {
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

private class BootstrapExchangePersistingAuthRepository(
	private val sessionRepository: SessionRepository
) : AuthRepository {
	var bootstrapCalls = 0
	var exchangeCalls = 0

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		bootstrapCalls++

		return BootstrapTokens(
			uid = "uid-1",
			usbId = usbId,
			accessToken = "bootstrap-access",
			expiresIn = 300
		)
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) {
		exchangeCalls++
		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "session-exchanged",
				accessToken = "access-exchanged",
				refreshToken = "refresh-exchanged",
				usbId = "12-34567"
			)
		)
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens = error("unused")

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}
