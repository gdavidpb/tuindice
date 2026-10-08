package com.gdavidpb.tuindice.auth.presentation.machine

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeAuthRetryWindowRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.exception.ServiceRetryWindowException
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventNames
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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
		val fixture = createFixture(testScheduler = testScheduler)
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
	fun toggleIdentifierMode_preservesValidUsbId_andClearsEmailWhenReturningToUsbId() = runTest {
		val fixture = createFixture(testScheduler = testScheduler)
		val viewModel = fixture.viewModel
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.toggleIdentifierModeAction()
				awaitUntilState<SignIn.State.Idle> { state ->
					state.usbId == VALID_USB_ID &&
						state.identifierMode == SignInIdentifierMode.UsbEmail
				}

				viewModel.setUsbIdAction("mail")
				viewModel.toggleIdentifierModeAction()
				awaitUntilState<SignIn.State.Idle> { state ->
					state.usbId.isEmpty() &&
						state.identifierMode == SignInIdentifierMode.UsbId
				}

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun clickSignIn_whileLoggingIn_isIgnored_andUseCaseRunsOnce() = runTest {
		val fixture = createFixture(testScheduler = testScheduler)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction(PASSWORD)
				viewModel.signInAction()
				awaitUntilState<SignIn.State.LoggingIn>()

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				// Second click while LoggingIn: invalid transition, must not re-run the
				// use case. The consent change behind it is valid from LoggingIn, so its
				// state emission is the FIFO anchor proving the click was already handled.
				viewModel.signInAction()
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
		val fixture = createFixture(testScheduler = testScheduler)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction(PASSWORD)
				viewModel.signInAction()
				awaitUntilState<SignIn.State.LoggingIn>()

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				assertIs<SignIn.Effect.NavigateToSummary>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
			advanceUntilIdle()

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

		assertMachineHasNoShadowedRows(
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
		val diagram = fixture.viewModel.machine.exportToMermaid(
			machineName = "sign_in",
			initialState = SignIn.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"logging_in",
			"ClickSignIn",
			"ClickCancelSignIn",
			"SignInSucceeded / NavigateToSummary",
			"OutdatedAppDetected / ShowOutdatedApp",
			"SignInFailed / ShowSnackBar · ShowRetrySnackBar",
			"SetUsbId",
			"ToggleIdentifierMode",
			"ClickTermsAndConditions / NavigateToBrowser"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun unavailableWithAWait_returnsToIdleDisabled_andReenablesWhenTheWaitElapses() = runTest {
		val fixture = createFixture(
			testScheduler = testScheduler,
			signInThrowable = ServiceRetryWindowException(retryAfterMillis = 30_000L),
			serviceWaitMillis = 30_000L
		)
		val viewModel = fixture.viewModel
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignIn.State.Idle(), awaitItem())

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction(PASSWORD)
				viewModel.signInAction()

				val waiting = awaitUntilState<SignIn.State.Idle> { state -> state.isServiceUnavailable }
				assertEquals(VALID_USB_ID, waiting.usbId)
				assertEquals(PASSWORD, waiting.password)

				advanceTimeBy(29_000L)
				assertTrue(viewModel.state.value.let { it is SignIn.State.Idle && it.isServiceUnavailable })

				advanceTimeBy(2_000L)
				awaitUntilState<SignIn.State.Idle> { state -> !state.isServiceUnavailable }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// Uses the wait branch of failSignIn: the other branch resolves a string resource, which only
	// iOS can do (see SignInViewModelContractTest.signInRejected_marksTheLastAttemptAsFailed).
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun aServiceWait_isNotARejectionOfTheLastAttempt() = runTest {
		val fixture = createFixture(
			testScheduler = testScheduler,
			signInThrowable = ServiceRetryWindowException(retryAfterMillis = 30_000L),
			serviceWaitMillis = 30_000L
		)
		val viewModel = fixture.viewModel
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(null, (awaitItem() as SignIn.State.Idle).rejection)

				viewModel.setUsbIdAction(VALID_USB_ID)
				viewModel.setPasswordAction(PASSWORD)
				viewModel.signInAction()

				val waiting = awaitUntilState<SignIn.State.Idle> { state -> state.isServiceUnavailable }
				assertEquals(VALID_USB_ID, waiting.usbId)
				assertEquals(PASSWORD, waiting.password)
				assertEquals(null, waiting.rejection)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// The marks are set by failSignIn, which only iOS can run; the rows that clear or keep them
	// are plain table rows, so they are checked from a state that already carries the mark.
	@Test
	fun editingEitherField_orTheMode_clearsTheFailedMark() = runTest {
		val machine = createFixture().viewModel.machine
		val verdicts = listOf(
			SignIn.Rejection.InvalidCredentials(message = "Revisa tu USBID"),
			SignIn.Rejection.AccountDisabled(message = "Cuenta inhabilitada"),
			SignIn.Rejection.Untrusted(message = "Dispositivo no verificado")
		)

		val edits = listOf(
			SignIn.Action.SetPassword(password = "${PASSWORD}x"),
			SignIn.Action.SetUsbId(usbId = "20-26124"),
			SignIn.Action.ToggleIdentifierMode
		)

		for (verdict in verdicts) {
			val rejected = SignIn.State.Idle(usbId = VALID_USB_ID, password = PASSWORD, rejection = verdict)

			for (edit in edits) {
				val after = assertIs<SignIn.State.Idle>(machine.nextState(rejected, edit))

				assertEquals(null, after.rejection, "${edit::class.simpleName} must clear ${verdict::class.simpleName}")
			}
		}
	}

	@Test
	fun theWaitElapsing_keepsTheFailedMark_andReenablesSignIn() = runTest {
		val machine = createFixture().viewModel.machine
		val verdict = SignIn.Rejection.AccountDisabled(message = "Cuenta inhabilitada")
		val waiting = SignIn.State.Idle(isServiceUnavailable = true, rejection = verdict)

		val after = assertIs<SignIn.State.Idle>(
			machine.nextState(waiting, SignInInternalEvent.ServiceWaitElapsed)
		)

		assertEquals(false, after.isServiceUnavailable)
		assertEquals(verdict, after.rejection)
	}

	@Test
	fun cancellingTheSignIn_returnsToIdleWithoutAMark() = runTest {
		val machine = createFixture().viewModel.machine
		val loggingIn = SignIn.State.LoggingIn(
			usbId = VALID_USB_ID,
			password = PASSWORD,
			messages = emptyList()
		)

		val after = assertIs<SignIn.State.Idle>(
			machine.nextState(loggingIn, SignIn.Action.ClickCancelSignIn)
		)

		assertEquals(null, after.rejection)
		assertEquals(VALID_USB_ID, after.usbId)
	}

	// The system autofill and a paste hand the identifier over in one change. A text with an @ can
	// only be an email, so the field leaves the USB ID mode and keeps what it was given.
	@Test
	fun anIdentifierWithAnAt_inUsbIdMode_switchesToEmailModeKeepingTheText() = runTest {
		val machine = createFixture().viewModel.machine

		for (identifier in listOf("mail@usb.ve", "12-34567@usb.ve", "@")) {
			val after = assertIs<SignIn.State.Idle>(
				machine.nextState(SignIn.State.Idle(), SignIn.Action.SetUsbId(usbId = identifier))
			)

			assertEquals(SignInIdentifierMode.UsbEmail, after.identifierMode, identifier)
			assertEquals(identifier, after.usbId)
		}
	}

	@Test
	fun anIdentifierWithoutAnAt_inUsbIdMode_staysInUsbIdMode() = runTest {
		val machine = createFixture().viewModel.machine

		for (identifier in listOf("", "1234567", "12-34567", "12-3")) {
			val after = assertIs<SignIn.State.Idle>(
				machine.nextState(SignIn.State.Idle(), SignIn.Action.SetUsbId(usbId = identifier))
			)

			assertEquals(SignInIdentifierMode.UsbId, after.identifierMode, identifier)
			assertEquals(identifier, after.usbId)
		}
	}

	@Test
	fun inEmailMode_anyIdentifierKeepsTheMode() = runTest {
		val machine = createFixture().viewModel.machine
		val email = SignIn.State.Idle(identifierMode = SignInIdentifierMode.UsbEmail)

		for (identifier in listOf("", "mail", "12-34567", "mail@usb.ve")) {
			val after = assertIs<SignIn.State.Idle>(
				machine.nextState(email, SignIn.Action.SetUsbId(usbId = identifier))
			)

			assertEquals(SignInIdentifierMode.UsbEmail, after.identifierMode, identifier)
			assertEquals(identifier, after.usbId)
		}
	}

	@Test
	fun theAutomaticSwitch_keepsThePassword_andClearsTheFailedMark() = runTest {
		val machine = createFixture().viewModel.machine
		val rejected = SignIn.State.Idle(
			password = PASSWORD,
			isPasswordVisible = true,
			rejection = SignIn.Rejection.InvalidCredentials(message = "Revisa tu USBID")
		)

		val after = assertIs<SignIn.State.Idle>(
			machine.nextState(rejected, SignIn.Action.SetUsbId(usbId = "mail@usb.ve"))
		)

		assertEquals(SignInIdentifierMode.UsbEmail, after.identifierMode)
		assertEquals(PASSWORD, after.password)
		assertEquals(true, after.isPasswordVisible)
		assertEquals(null, after.rejection)
	}

	@Test
	fun togglingByHand_stillClearsAnEmailThatIsNotAUsbId() = runTest {
		val machine = createFixture().viewModel.machine
		val email = SignIn.State.Idle(usbId = "mail@usb.ve", identifierMode = SignInIdentifierMode.UsbEmail)

		val after = assertIs<SignIn.State.Idle>(machine.nextState(email, SignIn.Action.ToggleIdentifierMode))

		assertEquals(SignInIdentifierMode.UsbId, after.identifierMode)
		assertEquals("", after.usbId)
	}

	@Test
	fun anIdentifierChangeWhileLoggingIn_isIgnored() = runTest {
		val machine = createFixture().viewModel.machine
		val loggingIn = SignIn.State.LoggingIn(usbId = VALID_USB_ID, password = PASSWORD, messages = emptyList())

		val result = machine.process(loggingIn, SignIn.Action.SetUsbId(usbId = "mail@usb.ve"))

		assertIs<TransitionResult.Rejected<SignIn.State>>(result)
	}

	private suspend fun MachineDefinition<SignIn.State>.nextState(state: SignIn.State, event: Any): SignIn.State {
		val result = process(state, event)

		assertIs<TransitionResult.Transitioned<SignIn.State>>(result)

		return result.toState
	}

	private fun createFixture(
		testScheduler: TestCoroutineScheduler? = null,
		signInThrowable: Throwable? = null,
		serviceWaitMillis: Long = 0L
	): SignInStateMachineFixture {
		val authRepository = RecordingAuthRepository(throwable = signInThrowable)
		val eventPublisher = RecordingEventPublisher()
		val dispatchers: TuIndiceDispatchers = testScheduler
			?.let { scheduler -> TestTuIndiceDispatchers(UnconfinedTestDispatcher(scheduler)) }
			?: DefaultTuIndiceDispatchers

		val viewModel = SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = SignInUseCase(
					authRepository = authRepository,
					authRetryWindowRepository = FakeAuthRetryWindowRepository(signInWaitMillis = serviceWaitMillis),
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
			eventPublisher = eventPublisher,
			dispatchers = dispatchers
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
