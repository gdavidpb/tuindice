package com.gdavidpb.tuindice.auth.presentation.machine

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventNames
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SignInStateMachineContractTest {
	private companion object {
		const val VALID_USB_ID = "20-26123"
		const val PASSWORD = "secret123"
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun payloadEvents_applyInSendOrder_withoutLoss() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.setUsbIdAction("1")
				viewModel.setUsbIdAction("12")
				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction(PASSWORD)
				viewModel.togglePasswordVisibilityAction()

				awaitUntilState<SignIn.State.Idle> { state ->
					state.usbId == VALID_USB_ID &&
						state.password == PASSWORD &&
						state.isPasswordVisible
				}

				cancelAndIgnoreRemainingEvents()
			}

			val actionNames = fixture.eventPublisher.events()
				.filter { event -> event.name == EventNames.APP_ACTION }
				.mapNotNull { event -> event.parameters[EventParameterKeys.ACTION] }

			assertEquals(
				listOf(
					"set_usb_id",
					"set_usb_id",
					"set_usb_id",
					"set_password",
					"toggle_password_visibility"
				),
				actionNames
			)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun clickSignIn_whileLoggingIn_isIgnored_andUseCaseRunsOnce() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.signInAction(VALID_USB_ID, PASSWORD)
				assertIs<SignIn.State.LoggingIn>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				// Second click while LoggingIn: invalid transition, must not re-run the
				// use case. The consent change behind it is valid from LoggingIn, so its
				// state emission is the FIFO anchor proving the click was already handled.
				viewModel.signInAction(VALID_USB_ID, PASSWORD)
				viewModel.setUsageDataCollectionEnabledAction(true)

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.state.test {
				awaitUntilState<SignIn.State.LoggingIn> { state ->
					state.usageDataCollectionEnabled
				}

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(1, fixture.authRepository.bootstrapSignInCalls.size)

			val invalidTransitions = fixture.eventPublisher.events()
				.filter { event -> event.name == EventNames.APP_INVALID_TRANSITION }

			assertEquals(1, invalidTransitions.size)
			assertEquals(
				"logging_in",
				invalidTransitions.single().parameters[EventParameterKeys.FROM]
			)
			assertEquals(
				"click_sign_in",
				invalidTransitions.single().parameters[EventParameterKeys.EVENT]
			)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun validTransitions_publishTransitionTelemetry() = runTest {
		val fixture = createFixture()
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.signInAction(VALID_USB_ID, PASSWORD)
				assertIs<SignIn.State.LoggingIn>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			val transitions = fixture.eventPublisher.events()
				.filter { event -> event.name == EventNames.APP_TRANSITION }
				.map { event ->
					Triple(
						event.parameters[EventParameterKeys.FROM],
						event.parameters[EventParameterKeys.EVENT],
						event.parameters[EventParameterKeys.TO]
					)
				}

			assertTrue(
				Triple("idle", "click_sign_in", "logging_in") in transitions,
				"Expected idle --click_sign_in--> logging_in in $transitions"
			)
			assertTrue(
				Triple("logging_in", "sign_in_succeeded", "logging_in") in transitions,
				"Expected logging_in --sign_in_succeeded--> logging_in in $transitions"
			)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun machine_coversTheFullInputAlphabet() {
		val fixture = createFixture()

		assertMachineCoversAlphabet(
			fixture.viewModel.machine,
			SignIn.Action::class,
			SignInInternalEvent::class
		)
	}

	@Test
	fun machine_declaresTheFullOutputAlphabet() {
		val fixture = createFixture()

		assertMachineCoversEffects(
			fixture.viewModel.machine,
			SignIn.Effect::class
		)
	}

	@Test
	fun machine_statesAreReachableFromIdle() {
		val fixture = createFixture()

		assertMachineStatesReachable(
			machine = fixture.viewModel.machine,
			initialState = SignIn.State.Idle::class
		)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val fixture = createFixture()
		val diagram = fixture.viewModel.exportMachineToMermaid()

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"logging_in",
			"ClickSignIn",
			"SignInSucceeded / NavigateToSummary",
			"SignInFailed / ShowSnackBar · ShowRetrySnackBar",
			"SetUsbId",
			"ClickTermsAndConditions / NavigateToBrowser"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createFixture(): SignInStateMachineFixture {
		val authRepository = RecordingAuthRepository()
		val eventPublisher = RecordingEventPublisher()

		val viewModel = SignInViewModel(
			signInMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = authRepository,
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
			eventPublisher = eventPublisher
		)

		return SignInStateMachineFixture(
			viewModel = viewModel,
			authRepository = authRepository,
			eventPublisher = eventPublisher
		)
	}
}

private data class SignInStateMachineFixture(
	val viewModel: SignInViewModel,
	val authRepository: RecordingAuthRepository,
	val eventPublisher: RecordingEventPublisher
)

private class RecordingEventPublisher : EventPublisher {
	private val channel = Channel<AppEvent>(Channel.UNLIMITED)
	private val received = mutableListOf<AppEvent>()

	override fun publish(event: AppEvent) {
		channel.trySend(event)
	}

	// Drain on the test coroutine only, so the backing list is single-threaded.
	fun events(): List<AppEvent> {
		while (true) {
			val result = channel.tryReceive()
			if (result.isSuccess) received += result.getOrThrow() else break
		}
		return received.toList()
	}
}
