package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.TogglePasswordVisibilityActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
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
			signInActionProcessor = SignInActionProcessor(
				signInUseCase = SignInUseCase(
					authRepository = RecordingAuthRepository(),
					messagingRepository = RecordingMessagingRepository(),
					syncRepository = FakeSyncRepository(),
					credentialsRepository = FakeCredentialsRepository(),
					riskAttestationRepository = FakeAttestationRepository(),
					paramsValidator = SignInParamsValidator(),
					exceptionHandler = SignInExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				),
				configRepository = FakeConfigRepository()
			),
			setUsbIdActionProcessor = SetUsbIdActionProcessor(),
			setPasswordActionProcessor = SetPasswordActionProcessor(),
			togglePasswordVisibilityActionProcessor = TogglePasswordVisibilityActionProcessor(),
			openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
				appEnvironmentRepository = FakeAppEnvironmentRepository()
			),
			privacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
				appEnvironmentRepository = FakeAppEnvironmentRepository()
			)
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

				viewModel.signInAction(VALID_USB_ID, "secret123")
				assertIs<SignIn.State.LoggingIn>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				viewModel.openTermsAndConditionsAction()
				val browserEffect = assertIs<SignIn.Effect.NavigateToBrowser>(awaitItem())
				assertEquals("https://tuindice.app/terms", browserEffect.url)

				viewModel.signInAction(VALID_USB_ID, "secret123")
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}
}
