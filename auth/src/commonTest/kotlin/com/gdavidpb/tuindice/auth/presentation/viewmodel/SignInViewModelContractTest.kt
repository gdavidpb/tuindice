package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeAuthRetryWindowRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignInViewModelContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun cancelWhileLoggingIn_restoresIdlePreservingInput() = runTest {
		val viewModel = SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = RecordingAuthRepository(),
					authRetryWindowRepository = FakeAuthRetryWindowRepository(),
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
				),
				configRepository = FakeConfigRepository(),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.setUsbIdAction(VALID_USB_ID)
				awaitItem()

				viewModel.setPasswordAction("secret123")
				awaitItem()

				viewModel.signInAction()
				assertIs<SignIn.State.LoggingIn>(awaitItem())

				viewModel.cancelSignInAction()
				assertEquals(
					SignIn.State.Idle(
						usbId = VALID_USB_ID,
						password = "secret123"
					),
					awaitItem()
				)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signInRejected_marksTheLastAttemptAsFailed() = runTest {
		val viewModel = SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = RecordingAuthRepository(
						throwable = clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v2/bootstrap")
					),
					authRetryWindowRepository = FakeAuthRetryWindowRepository(),
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
				),
				configRepository = FakeConfigRepository(),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				awaitItem()

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction("secret123")
				viewModel.signInAction()

				val rejected = awaitUntilState<SignIn.State.Idle> { state -> state.lastAttemptFailed }
				assertEquals(VALID_USB_ID, rejected.usbId)
				assertEquals("secret123", rejected.password)

				viewModel.setPasswordAction("secret1234")
				awaitUntilState<SignIn.State.Idle> { state -> !state.lastAttemptFailed }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signInFailingOnTheWayToTheBackend_doesNotMarkTheLastAttemptAsRejected() = runTest {
		val authRepository = RecordingAuthRepository(
			throwable = serverResponseException(HttpStatusCode.ServiceUnavailable, path = "/auth/v2/bootstrap")
		)
		val viewModel = SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = authRepository,
					authRetryWindowRepository = FakeAuthRetryWindowRepository(),
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
				),
				configRepository = FakeConfigRepository(),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				awaitItem()

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction("secret123")
				viewModel.signInAction()

				// The attempt reached the backend and came back to the form without a rejection.
				val back = awaitUntilState<SignIn.State.Idle> { _ -> authRepository.bootstrapSignInCalls.isNotEmpty() }
				assertEquals(false, back.lastAttemptFailed)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun publicActions_updateState_andEmitEffects() = runTest {
		val viewModel = SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = RecordingAuthRepository(),
					authRetryWindowRepository = FakeAuthRetryWindowRepository(),
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
				),
				configRepository = FakeConfigRepository(),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.togglePasswordVisibilityAction()
				assertEquals(SignIn.State.Idle(isPasswordVisible = true), awaitItem())

				viewModel.setUsbIdAction(VALID_USB_ID)
				assertEquals(
					SignIn.State.Idle(
						usbId = VALID_USB_ID,
						isPasswordVisible = true
					),
					awaitItem()
				)

				viewModel.setPasswordAction("secret123")
				assertEquals(
					SignIn.State.Idle(
						usbId = VALID_USB_ID,
						password = "secret123",
						isPasswordVisible = true
					),
					awaitItem()
				)

				viewModel.toggleIdentifierModeAction()
				assertEquals(
					SignIn.State.Idle(
						usbId = VALID_USB_ID,
						password = "secret123",
						identifierMode = SignInIdentifierMode.UsbEmail,
						isPasswordVisible = true
					),
					awaitItem()
				)

				viewModel.toggleIdentifierModeAction()
				assertEquals(
					SignIn.State.Idle(
						usbId = VALID_USB_ID,
						password = "secret123",
						isPasswordVisible = true
					),
					awaitItem()
				)

				viewModel.signInAction()
				assertIs<SignIn.State.LoggingIn>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				viewModel.openTermsAndConditionsAction()
				val browserEffect = assertIs<SignIn.Effect.NavigateToBrowser>(awaitItem())
				assertEquals("https://tuindice.app/terms", browserEffect.url)

				// Re-clicking sign-in while LoggingIn is an invalid transition: the machine
				// ignores it, so no second NavigateToSummary may be emitted. Both events are
				// queued in order; if the re-click were processed, its NavigateToSummary
				// would arrive before the browser effect asserted below.
				viewModel.signInAction()
				viewModel.openPrivacyPolicyAction()
				assertIs<SignIn.Effect.NavigateToBrowser>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}
}
