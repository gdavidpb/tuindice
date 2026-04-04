package com.gdavidpb.tuindice.auth.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
		assertEquals("bootstrap-access-token", attestationRepository.lastRequest?.bearerToken)
		assertEquals("{}", attestationRepository.lastRequest?.payloadJson)
		assertEquals(1, messagingRepository.subscribeCalls)
		assertEquals(listOf("secret123"), credentialsRepository.storedPasswords)
		assertEquals(SyncStatus.Failed, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Failed), syncStatusRepository.setStatuses)
		assertEquals(listOf("secret123"), syncRepository.scheduledSyncCalls)
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
		val messagingRepository = RecordingMessagingRepository()
		val sessionRepository = FakeSessionRepository()
		val applicationRepository = RecordingApplicationRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val useCase = SignOutUseCase(
			authRepository = authRepository,
			sessionRepository = sessionRepository,
			messagingRepository = messagingRepository,
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, authRepository.revokeTokensCalls)
		assertEquals(listOf("access-token"), authRepository.revokedAccessTokens)
		assertEquals(true, sessionRepository.cleared)
		assertEquals(1, messagingRepository.unsubscribeCalls)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Healthy), syncStatusRepository.setStatuses)
		assertEquals(true, applicationRepository.cleared)
	}
}
