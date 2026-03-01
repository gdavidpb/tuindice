package com.gdavidpb.tuindice.login.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.login.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.login.testing.RecordingDependenciesRepository
import com.gdavidpb.tuindice.login.testing.RecordingLoginRepository
import com.gdavidpb.tuindice.login.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginUseCaseContractTest {
	@Test
	fun signInUseCase_emitsLoadingThenData_andDelegatesToRepository() = runTest {
		val repository = RecordingLoginRepository()
		val attestationRepository = FakeAttestationRepository()
		val useCase = SignInUseCase(
			loginRepository = repository,
			attestationRepository = attestationRepository,
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(SignInParams(usbId = "20261234", password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.signInCalls.size)
	}

	@Test
	fun updatePasswordUseCase_emitsLoadingThenData_andUsesStoredUsbId() = runTest {
		val repository = RecordingLoginRepository()
		val sessionRepository = FakeSessionRepository(usbId = "20261234")
		val useCase = UpdatePasswordUseCase(
			loginRepository = repository,
			sessionRepository = sessionRepository,
			attestationRepository = FakeAttestationRepository(),
			paramsValidator = UpdatePasswordParamsValidator(),
			exceptionHandler = UpdatePasswordExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute("new-secret").test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals("20261234", repository.updatePasswordCalls.single().first)
	}

	@Test
	fun signOutUseCase_emitsLoadingThenData_andClearsDependencies() = runTest {
		val messagingRepository = RecordingMessagingRepository()
		val sessionRepository = FakeSessionRepository()
		val applicationRepository = RecordingApplicationRepository()
		val dependenciesRepository = RecordingDependenciesRepository()
		val useCase = SignOutUseCase(
			sessionRepository = sessionRepository,
			messagingRepository = messagingRepository,
			applicationRepository = applicationRepository,
			dependenciesRepository = dependenciesRepository
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(true, sessionRepository.cleared)
		assertEquals(1, messagingRepository.unsubscribeCalls)
		assertEquals(true, applicationRepository.cleared)
		assertEquals(1, dependenciesRepository.restartCalls)
	}
}
