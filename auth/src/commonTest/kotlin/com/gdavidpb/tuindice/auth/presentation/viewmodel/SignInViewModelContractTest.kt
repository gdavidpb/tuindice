package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
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
	fun publicActions_updateState_andEmitEffects() = runTest {
		val viewModel = SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = RecordingAuthRepository(),
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

				viewModel.signInAction()
				assertIs<SignIn.State.LoggingIn>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				viewModel.openTermsAndConditionsAction()
				val browserEffect = assertIs<SignIn.Effect.NavigateToBrowser>(awaitItem())
				assertEquals("https://tuindice.app/terms_and_conditions_v6_0.html", browserEffect.url)

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
