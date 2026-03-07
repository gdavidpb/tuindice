package com.gdavidpb.tuindice.login.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.RiskOperation
import com.gdavidpb.tuindice.login.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.login.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.login.testing.RecordingLoginRepository
import com.gdavidpb.tuindice.login.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginUseCaseContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
	}

	@Test
	fun signInUseCase_emitsLoadingThenData_andDelegatesToRepository() = runTest {
		val repository = RecordingLoginRepository()
		val attestationRepository = FakeAttestationRepository()
		val messagingRepository = RecordingMessagingRepository()
		val useCase = SignInUseCase(
			loginRepository = repository,
			messagingRepository = messagingRepository,
			riskAttestationRepository = attestationRepository,
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(SignInParams(usbId = VALID_USB_ID, password = "secret123")).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.issueTokensCalls.size)
		assertEquals(IssueTokensFlow.IssueTokens, repository.issueTokensCalls.single().flow)
		assertEquals(RiskOperation.IssueTokens, attestationRepository.lastRequest?.operation)
		assertEquals(1, messagingRepository.subscribeCalls)
	}

	@Test
	fun updatePasswordUseCase_emitsLoadingThenData_andUsesStoredUsbId() = runTest {
		val repository = RecordingLoginRepository()
		val sessionRepository = FakeSessionRepository(usbId = "20261234")
		val useCase = UpdatePasswordUseCase(
			loginRepository = repository,
			sessionRepository = sessionRepository,
			riskAttestationRepository = FakeAttestationRepository(),
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

		val call = repository.issueTokensCalls.single()
		assertEquals("20261234", call.usbId)
		assertEquals(IssueTokensFlow.ReissueTokens, call.flow)
	}

	@Test
	fun signOutUseCase_emitsLoadingThenData_andClearsSessionData() = runTest {
		val messagingRepository = RecordingMessagingRepository()
		val sessionRepository = FakeSessionRepository()
		val applicationRepository = RecordingApplicationRepository()
		val useCase = SignOutUseCase(
			sessionRepository = sessionRepository,
			messagingRepository = messagingRepository,
			applicationRepository = applicationRepository
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(true, sessionRepository.cleared)
		assertEquals(1, messagingRepository.unsubscribeCalls)
		assertEquals(true, applicationRepository.cleared)
	}
}
