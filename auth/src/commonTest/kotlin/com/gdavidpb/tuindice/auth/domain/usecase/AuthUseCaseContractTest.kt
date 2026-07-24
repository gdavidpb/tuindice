package com.gdavidpb.tuindice.auth.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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
			settingsRepository = FakeSettingsRepository(),
			applicationRepository = RecordingApplicationRepository(),
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
	fun signInUseCase_completesSignIn_whenMessagingSubscriptionFails() = runTest {
		val subscribeThrowable = IllegalStateException("FCM registration failed")
		val repository = RecordingAuthRepository()
		val messagingRepository = FakeMessagingRepository(throwable = subscribeThrowable)
		val syncRepository = FakeSyncRepository()
		val credentialsRepository = FakeCredentialsRepository()
		val reportingRepository = RecordingReportingRepository()
		val useCase = SignInUseCase(
			authRepository = repository,
			messagingRepository = messagingRepository,
			syncRepository = syncRepository,
			credentialsRepository = credentialsRepository,
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = FakeAttestationRepository(),
			settingsRepository = FakeSettingsRepository(),
			applicationRepository = RecordingApplicationRepository(),
			reportingRepository = reportingRepository,
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute(SignInParams(usbId = VALID_USB_ID, password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, messagingRepository.subscribeCalls)
		assertEquals(listOf("secret123"), credentialsRepository.storedPasswords)
		assertEquals(listOf("secret123"), syncRepository.scheduledSyncCalls)
		assertEquals(subscribeThrowable, reportingRepository.loggedExceptions.single())
		assertEquals(true, reportingRepository.customKeys["is-handled"])
		assertEquals("SignInUseCase", reportingRepository.customKeys["use-case"])
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
			settingsRepository = FakeSettingsRepository(),
			applicationRepository = RecordingApplicationRepository(),
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
	fun signInUseCase_canonicalizesUsbIdInUsbEmailModeBeforeCallingRepository() = runTest {
		val repository = RecordingAuthRepository()
		val useCase = SignInUseCase(
			authRepository = repository,
			messagingRepository = RecordingMessagingRepository(),
			syncRepository = FakeSyncRepository(),
			credentialsRepository = FakeCredentialsRepository(),
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = FakeAttestationRepository(),
			settingsRepository = FakeSettingsRepository(),
			applicationRepository = RecordingApplicationRepository(),
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute(
			SignInParams(
				usbId = "2026123@usb.ve",
				password = "secret123",
				identifierMode = SignInIdentifierMode.UsbEmail
			)
		).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals("20-26123", repository.bootstrapSignInCalls.single().usbId)
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
			settingsRepository = FakeSettingsRepository(),
			applicationRepository = RecordingApplicationRepository(),
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
		val attestationRepository = FakeAttestationRepository()
		val useCase = UpdatePasswordUseCase(
			authRepository = repository,
			sessionRepository = sessionRepository,
			syncRepository = syncRepository,
			credentialsRepository = credentialsRepository,
			syncStatusRepository = syncStatusRepository,
			attestationRepository = attestationRepository,
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
		assertEquals(AttestationAuthorization.CurrentSession, attestationRepository.lastRequest?.authorization)
		assertEquals(listOf("new-secret"), credentialsRepository.storedPasswords)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Failed), syncStatusRepository.setStatuses)
		assertEquals(listOf("new-secret"), syncRepository.scheduledSyncCalls)
	}

	@Test
	fun signInUseCase_whenLocalDataBelongsToAnotherIdentity_clearsItBeforeSigningIn() = runTest {
		val settingsRepository = FakeSettingsRepository(localDataOwner = "20-19999")
		val applicationRepository = RecordingApplicationRepository()
		val useCase = signInUseCase(
			settingsRepository = settingsRepository,
			applicationRepository = applicationRepository
		)

		useCase.execute(SignInParams(usbId = VALID_USB_ID, password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(true, applicationRepository.cleared)
		assertEquals(VALID_USB_ID, settingsRepository.getLocalDataOwner())
	}

	@Test
	fun signInUseCase_whenLocalDataBelongsToSameIdentity_keepsIt() = runTest {
		val settingsRepository = FakeSettingsRepository(localDataOwner = VALID_USB_ID)
		val applicationRepository = RecordingApplicationRepository()
		val useCase = signInUseCase(
			settingsRepository = settingsRepository,
			applicationRepository = applicationRepository
		)

		useCase.execute(SignInParams(usbId = VALID_USB_ID, password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(false, applicationRepository.cleared)
		assertEquals(VALID_USB_ID, settingsRepository.getLocalDataOwner())
	}

	@Test
	fun signInUseCase_whenNoOwnerRecorded_keepsDataAndRecordsOwner() = runTest {
		val settingsRepository = FakeSettingsRepository()
		val applicationRepository = RecordingApplicationRepository()
		val useCase = signInUseCase(
			settingsRepository = settingsRepository,
			applicationRepository = applicationRepository
		)

		useCase.execute(SignInParams(usbId = VALID_USB_ID, password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(false, applicationRepository.cleared)
		assertEquals(VALID_USB_ID, settingsRepository.getLocalDataOwner())
	}

	private fun signInUseCase(
		settingsRepository: FakeSettingsRepository,
		applicationRepository: RecordingApplicationRepository
	) = SignInUseCase(
		authRepository = RecordingAuthRepository(),
		messagingRepository = RecordingMessagingRepository(),
		syncRepository = FakeSyncRepository(),
		credentialsRepository = FakeCredentialsRepository(),
		syncStatusRepository = FakeSyncStatusRepository(),
		attestationRepository = FakeAttestationRepository(),
		settingsRepository = settingsRepository,
		applicationRepository = applicationRepository,
		reportingRepository = RecordingReportingRepository(),
		paramsValidator = SignInParamsValidator(),
		exceptionHandler = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)
	)

	@Test
	fun signOutUseCase_emitsLoadingThenData_andClearsSessionData() = runTest {
		val authRepository = RecordingAuthRepository()
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val applicationRepository = RecordingApplicationRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		var activeSessionWorkCancelled = false
		val activeSessionWork = sessionCoroutineScope.launch {
			try {
				awaitCancellation()
			} finally {
				activeSessionWorkCancelled = true
			}
		}
		val useCase = SignOutUseCase(
			authRepository = authRepository,
			attestationRepository = attestationRepository,
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			sessionCoroutineScope = sessionCoroutineScope,
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
		assertTrue(activeSessionWork.isCancelled)
		assertTrue(activeSessionWorkCancelled)
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
			sessionCoroutineScope = testSessionCoroutineScope(),
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
