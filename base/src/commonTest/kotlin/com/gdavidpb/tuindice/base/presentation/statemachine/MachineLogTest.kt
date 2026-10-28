package com.gdavidpb.tuindice.base.presentation.statemachine

import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.logging.MachineLog
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MachineLogTest {
	private val lines = MutableStateFlow<List<String>>(emptyList())

	@AfterTest
	fun tearDown() {
		MachineLog.sink = null
	}

	@Test
	fun when_noSinkIsSet_then_nothingIsFormatted() {
		var formatted = false

		MachineLog.write(MACHINE) {
			formatted = true
			"line"
		}

		assertFalse(formatted)
	}

	@Test
	fun when_aLineIsTooLong_then_itIsWrittenInNumberedPieces() {
		MachineLog.sink = { line -> lines.value += line }

		MachineLog.write(MACHINE) { "x".repeat(LONG_LINE_LENGTH) }

		assertEquals(2, lines.value.size)
		assertTrue(lines.value[0].startsWith("[$MACHINE] (1/2) "))
		assertTrue(lines.value[1].startsWith("[$MACHINE] (2/2) "))
	}

	@Test
	fun when_theMachineRuns_then_eachOperationIsLoggedWithItsData() = runTest {
		MachineLog.sink = { line -> lines.value += line }

		withMainDispatcher { dispatchers ->
			val viewModel = LogTestViewModel(dispatchers = dispatchers)
			val stateJob = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.startAction(label = "first")
				viewModel.resetAction()

				withTimeout(TIMEOUT_MILLIS) {
					lines.first { written -> written.any { line -> "rejected" in line } }
				}
			} finally {
				stateJob.cancel()
			}
		}

		assertLogged("start in Idle")
		assertLogged("Idle + action Start(label=first) -> Started")
		assertLogged("state Started(label=first)")
		assertLogged("effect Begun")
		assertLogged("job 1 started in Idle")
		assertLogged("rejected action Reset in Started")
	}

	@Test
	fun when_aTransitionFails_then_itsFailureIsLogged() = runTest {
		MachineLog.sink = { line -> lines.value += line }

		withMainDispatcher { dispatchers ->
			val viewModel = LogTestViewModel(dispatchers = dispatchers)
			val stateJob = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.failAction()

				awaitLogLine("failed action Fail in Idle")
			} finally {
				stateJob.cancel()
			}
		}

		assertTrue(
			lines.value.any { line ->
				line.startsWith("[$MACHINE] failed action Fail in Idle: ") && FAILURE_MESSAGE in line
			},
			"Missing the failure of the transition in ${lines.value}"
		)
	}

	@Test
	fun when_aJobCompletes_then_itsEndIsLogged() = runTest {
		MachineLog.sink = { line -> lines.value += line }

		withMainDispatcher { dispatchers ->
			val viewModel = LogTestViewModel(dispatchers = dispatchers)
			val stateJob = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.quickAction()

				awaitLogLine("job 1 done")
			} finally {
				stateJob.cancel()
			}
		}

		assertLogged("job 1 started in Idle")
		assertLogged("job 1 done")
	}

	@Test
	fun when_theViewModelIsCleared_then_itsJobsEndAndTheClearingIsLogged() = runTest {
		MachineLog.sink = { line -> lines.value += line }

		withMainDispatcher { dispatchers ->
			val viewModel = LogTestViewModel(dispatchers = dispatchers)
			val stateJob = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.startAction(label = "first")
				awaitLogLine("-> Started")

				viewModel.closeForTest()
				awaitLogLine("job 1 cancelled")
			} finally {
				stateJob.cancel()
			}
		}

		assertLogged("cleared in Started")
		assertLogged("job 1 cancelled")
	}

	private suspend fun awaitLogLine(fragment: String) {
		withTimeout(TIMEOUT_MILLIS) {
			lines.first { written -> written.any { line -> fragment in line } }
		}
	}

	private fun assertLogged(message: String) {
		val expected = "[$MACHINE] $message"

		assertTrue(expected in lines.value, "Missing <$expected> in ${lines.value}")
	}

	private companion object {
		const val MACHINE = "log_test"
		const val LONG_LINE_LENGTH = 1_000
		const val TIMEOUT_MILLIS = 1_000L
	}
}

// The failing transition throws a CancellationException, which ends the machine's coroutine quietly.
// Any other exception would reach the uncaught-exception handler of viewModelScope instead, and fail the test scope.
private const val FAILURE_MESSAGE = "transition failed on purpose"

private class LogTestMachine : ScreenMachine<LogTestState, LogTestEffect> {
	override fun initialState(): LogTestState = LogTestState.Idle

	override fun define(host: MachineHost<LogTestEffect>): MachineDefinition<LogTestState> {
		return MachineDefinition.define {
			from<LogTestState.Idle> {
				onTo<LogTestAction.Start, LogTestState.Started>(
					emits = setOf(LogTestEffect.Begun::class)
				) { _, action ->
					host.launchMachineJob { awaitCancellation() }
					host.sendEffect(LogTestEffect.Begun)
					LogTestState.Started(label = action.label)
				}

				on<LogTestAction.Quick> { state, _ ->
					host.launchMachineJob { }
					state
				}

				onTo<LogTestAction.Fail, LogTestState.Idle> { _, _ ->
					throw CancellationException(FAILURE_MESSAGE)
				}
			}
		}
	}
}

private class LogTestViewModel(
	dispatchers: TuIndiceDispatchers
) : StateMachineViewModel<LogTestState, LogTestAction, LogTestEffect>(
	name = "log_test",
	initialState = LogTestState.Idle,
	dispatchers = dispatchers
) {
	override val eventPublisher: EventPublisher = object : EventPublisher {
		override fun publish(event: AppEvent) = Unit
	}

	override val screenMachine: ScreenMachine<LogTestState, LogTestEffect> = LogTestMachine()

	fun startAction(label: String) {
		sendAction(LogTestAction.Start(label = label))
	}

	fun resetAction() {
		sendAction(LogTestAction.Reset())
	}

	fun quickAction() {
		sendAction(LogTestAction.Quick())
	}

	fun failAction() {
		sendAction(LogTestAction.Fail())
	}

	// Clears the view model as its owner does: the machine logs the clearing, then its scope ends, and with it its jobs.
	fun closeForTest() {
		onCleared()
		viewModelScope.cancel()
	}
}

private sealed class LogTestState : ViewState {
	data object Idle : LogTestState()
	data class Started(val label: String) : LogTestState()
}

private sealed class LogTestAction : ViewAction {
	data class Start(val label: String) : LogTestAction()

	// Not a data class: the log names it by its class instead of printing its address.
	class Reset : LogTestAction()

	class Quick : LogTestAction()

	class Fail : LogTestAction()
}

private sealed class LogTestEffect : ViewEffect {
	data object Begun : LogTestEffect()
}
