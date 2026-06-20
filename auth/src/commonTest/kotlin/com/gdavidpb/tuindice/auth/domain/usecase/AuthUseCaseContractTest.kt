package com.gdavidpb.tuindice.auth.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthUseCaseContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
	}

	@Test
	fun signInUseCase_emitsLoadingThenData_andDelegatesToRepository() = runTest {
		val repository = RecordingAuthRepository()
		val attestationRepository = FakeAttestationRepository()
		val messagingRepository = RecordingMessagingRepository()
		val syncRepository = FakeSyncRepository()
		val credentialsRepository = FakeCredentialsRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val useCase = SignInUseCase(
			authRepository = repository,
			messagingRepository = messagingRepository,
			syncRepository = syncRepository,
			credentialsRepository = credentialsRepository,
			syncStatusRepository = syncStatusRepository,
			attestationRepository = attestationRepository,
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute(SignInParams(usbId = VALID_USB_ID, password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.bootstrapSignInCalls.size)
		assertEquals(1, repository.exchangeSignInCalls.size)
		assertEquals(VALID_USB_ID, repository.bootstrapSignInCalls.single().usbId)
		assertEquals("secret123", repository.bootstrapSignInCalls.single().password)
		assertEquals("bootstrap-access-token", repository.exchangeSignInCalls.single().bootstrapAccessToken)
		assertEquals(ProtectedOperationCodes.AuthExchange, attestationRepository.lastRequest?.operationCode)
		assertEquals(
			AttestationAuthorization.Bearer(accessToken = "bootstrap-access-token"),
			attestationRepository.lastRequest?.authorization
		)
		assertEquals("{}", attestationRepository.lastRequest?.payloadJson)
		assertEquals(1, messagingRepository.subscribeCalls)
		assertEquals(listOf("secret123"), credentialsRepository.storedPasswords)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Failed), syncStatusRepository.setStatuses)
		assertEquals(listOf("secret123"), syncRepository.scheduledSyncCalls)
	}

	@Test
	fun signInUseCase_canonicalizesUsbEmailBeforeCallingRepository() = runTest {
		val repository = RecordingAuthRepository()
		val useCase = SignInUseCase(
			authRepository = repository,
			messagingRepository = RecordingMessagingRepository(),
			syncRepository = FakeSyncRepository(),
			credentialsRepository = FakeCredentialsRepository(),
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = FakeAttestationRepository(),
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute(
			SignInParams(
				usbId = "Mail@USB.VE",
				password = "secret123",
				identifierMode = SignInIdentifierMode.UsbEmail
			)
		).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals("mail", repository.bootstrapSignInCalls.single().usbId)
	}

	@Test
	fun signInUseCase_canonicalizesCompactUsbIdBeforeCallingRepository() = runTest {
		val repository = RecordingAuthRepository()
		val useCase = SignInUseCase(
			authRepository = repository,
			messagingRepository = RecordingMessagingRepository(),
			syncRepository = FakeSyncRepository(),
			credentialsRepository = FakeCredentialsRepository(),
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = FakeAttestationRepository(),
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute(SignInParams(usbId = "2026123", password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals("20-26123", repository.bootstrapSignInCalls.single().usbId)
	}

	@Test
	fun updatePasswordUseCase_emitsLoadingThenData_andUsesStoredUsbId() = runTest {
		val repository = RecordingAuthRepository()
		val sessionRepository = FakeSessionRepository(usbId = "20261234")
		val syncRepository = FakeSyncRepository()
		val credentialsRepository = FakeCredentialsRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val useCase = UpdatePasswordUseCase(
			authRepository = repository,
			sessionRepository = sessionRepository,
			syncRepository = syncRepository,
			credentialsRepository = credentialsRepository,
			syncStatusRepository = syncStatusRepository,
			attestationRepository = FakeAttestationRepository(),
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute("new-secret").test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		val call = repository.reissueTokensCalls.single()
		assertEquals("20261234", call.usbId)
		assertEquals(listOf("new-secret"), credentialsRepository.storedPasswords)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Failed), syncStatusRepository.setStatuses)
		assertEquals(listOf("new-secret"), syncRepository.scheduledSyncCalls)
	}

	@Test
	fun signOutUseCase_emitsLoadingThenData_andClearsSessionData() = runTest {
		val authRepository = RecordingAuthRepository()
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val applicationRepository = RecordingApplicationRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val useCase = SignOutUseCase(
			authRepository = authRepository,
			attestationRepository = attestationRepository,
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, authRepository.revokeTokensCalls)
		assertEquals(listOf("session-123"), authRepository.revokedSessionIds)
		assertEquals(listOf("refresh-token"), authRepository.revokedRefreshTokens)
		assertEquals(ProtectedOperationCodes.AuthRevokeTokens, attestationRepository.lastRequest?.operationCode)
		assertEquals(
			AttestationAuthorization.Session(
				sessionId = "session-123",
				refreshToken = "refresh-token"
			),
			attestationRepository.lastRequest?.authorization
		)
		assertEquals(true, sessionRepository.cleared)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertEquals(1, syncStatusRepository.resetCalls)
		assertEquals(true, applicationRepository.cleared)
		assertEquals("session-123", sessionInvalidationRepository.intentionalSignOutSessionId)
	}

	@Test
	fun signOutUseCase_emitsError_when_revokeReturnsUnauthorized() = runTest {
		val revokeThrowable = clientRequestException(
			statusCode = HttpStatusCode.Unauthorized,
			path = "/auth/v2/token/revoke"
		)
		val authRepository = RecordingAuthRepository(throwable = revokeThrowable)
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val applicationRepository = RecordingApplicationRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val reportingRepository = RecordingReportingRepository()
		val useCase = SignOutUseCase(
			authRepository = authRepository,
			attestationRepository = attestationRepository,
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)
			awaitComplete()
		}

		assertEquals(1, authRepository.revokeTokensCalls)
		assertEquals(listOf("refresh-token"), authRepository.revokedRefreshTokens)
		assertEquals(false, sessionRepository.cleared)
		assertEquals(SyncStatus.OutdatedCredentials, syncStatusRepository.getSyncStatus())
		assertEquals(false, applicationRepository.cleared)
		assertEquals(null, sessionInvalidationRepository.intentionalSignOutSessionId)
		assertEquals(revokeThrowable, reportingRepository.loggedExceptions.single())
	}

	@Test
	fun confirmSignOutUseCase_emitsPendingChanges_whenLocalChangesExist() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 2,
			recordCount = 1,
			evaluationsCount = 1,
			hasFailedMutations = false
		)
		val authRepository = RecordingAuthRepository()
		val useCase = ConfirmSignOutUseCase(
			pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(pendingChanges, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(0, authRepository.revokeTokensCalls)
	}

	@Test
	fun flushPendingChangesUseCase_emitsPendingRemaining_whenPendingChangesRemain() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 3,
			recordCount = 2,
			evaluationsCount = 1,
			hasFailedMutations = true
		)
		val useCase = FlushPendingChangesUseCase(
			pendingChangesRepository = FakePendingChangesRepository(
				flushResult = FlushPendingChangesResult.PendingRemaining(pendingChanges)
			),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(
				FlushPendingChangesResult.PendingRemaining(pendingChanges),
				awaitLoadingThenData(this)
			)
			awaitComplete()
		}
	}
}
