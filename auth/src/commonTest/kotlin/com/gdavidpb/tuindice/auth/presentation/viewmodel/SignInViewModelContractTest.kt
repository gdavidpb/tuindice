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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_usb_id_credentials
import tuindice.auth.generated.resources.error_untrusted
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

	// The verdict is kept with the text that explains it, and the snackbar no longer carries it.
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signInRejected_keepsWhichVerdictItWas_withItsMessage_andSendsNoSnackbar() = runTest {
		val supportEmail = FakeConfigRepository().getContactEmail()

		val cases = listOf(
			clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v2/bootstrap") to
				SignIn.Rejection.InvalidCredentials(getString(Res.string.error_invalid_usb_id_credentials)),
			clientRequestException(HttpStatusCode.Locked, path = "/auth/v2/bootstrap") to
				SignIn.Rejection.AccountDisabled(getString(Res.string.error_account_disabled, supportEmail)),
			clientRequestException(HttpStatusCode.Forbidden, path = "/auth/v2/bootstrap") to
				SignIn.Rejection.Untrusted(getString(Res.string.error_untrusted, supportEmail))
		)

		for ((throwable, expected) in cases) {
			val viewModel = createViewModel(RecordingAuthRepository(throwable = throwable))
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

					val rejected = awaitUntilState<SignIn.State.Idle> { state -> state.rejection != null }
					assertEquals(expected, rejected.rejection)
					assertEquals(VALID_USB_ID, rejected.usbId)
					assertEquals("secret123", rejected.password)

					cancelAndIgnoreRemainingEvents()
				}

				// The effect goes out before the state returns to the form: if one was sent, it is queued.
				viewModel.effect.test {
					expectNoEvents()
					cancelAndIgnoreRemainingEvents()
				}
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun editingAfterARejection_clearsIt_whateverTheVerdict() = runTest {
		val throwables = listOf(
			clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v2/bootstrap"),
			clientRequestException(HttpStatusCode.Locked, path = "/auth/v2/bootstrap"),
			clientRequestException(HttpStatusCode.Forbidden, path = "/auth/v2/bootstrap")
		)

		for (throwable in throwables) {
			val viewModel = createViewModel(RecordingAuthRepository(throwable = throwable))
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
					awaitUntilState<SignIn.State.Idle> { state -> state.rejection != null }

					viewModel.setPasswordAction("secret1234")
					awaitUntilState<SignIn.State.Idle> { state -> state.rejection == null }

					cancelAndIgnoreRemainingEvents()
				}
			} finally {
				stateCollector.cancel()
			}
		}
	}

	// Throttling and every failure on the way keep their snackbar and leave no fixed signal.
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signInFailingWithoutAVerdict_keepsItsSnackbar_andLeavesNoRejection() = runTest {
		val cases = listOf(
			clientRequestException(HttpStatusCode.TooManyRequests, path = "/auth/v2/bootstrap") to
				SignIn.Effect.ShowSnackBar::class,
			serverResponseException(HttpStatusCode.ServiceUnavailable, path = "/auth/v2/bootstrap") to
				SignIn.Effect.ShowRetrySnackBar::class
		)

		for ((throwable, effectType) in cases) {
			val authRepository = RecordingAuthRepository(throwable = throwable)
			val viewModel = createViewModel(authRepository)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.effect.test {
					viewModel.setUsbIdAction(VALID_USB_ID)
					viewModel.setPasswordAction("secret123")
					viewModel.signInAction()

					assertEquals(effectType, awaitItem()::class)

					// The attempt reached the backend and came back to the form without a verdict.
					val back = viewModel.state.first { state ->
						state is SignIn.State.Idle && authRepository.bootstrapSignInCalls.isNotEmpty()
					}
					assertEquals(null, (back as SignIn.State.Idle).rejection)

					cancelAndIgnoreRemainingEvents()
				}
			} finally {
				stateCollector.cancel()
			}
		}
	}

	private fun createViewModel(authRepository: RecordingAuthRepository): SignInViewModel {
		return SignInViewModel(
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
