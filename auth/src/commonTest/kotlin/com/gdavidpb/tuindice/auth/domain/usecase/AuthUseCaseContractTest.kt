package com.gdavidpb.tuindice.auth.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.RiskOperation
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
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
		val useCase = SignInUseCase(
			authRepository = repository,
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
		val repository = RecordingAuthRepository()
		val sessionRepository = FakeSessionRepository(usbId = "20261234")
		val useCase = UpdatePasswordUseCase(
			authRepository = repository,
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
		val authRepository = RecordingAuthRepository()
		val messagingRepository = RecordingMessagingRepository()
		val sessionRepository = FakeSessionRepository()
		val applicationRepository = RecordingApplicationRepository()
		val useCase = SignOutUseCase(
			authRepository = authRepository,
			sessionRepository = sessionRepository,
			messagingRepository = messagingRepository,
			applicationRepository = applicationRepository
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, authRepository.revokeTokensCalls)
		assertEquals(true, sessionRepository.cleared)
		assertEquals(1, messagingRepository.unsubscribeCalls)
		assertEquals(true, applicationRepository.cleared)
	}
}
