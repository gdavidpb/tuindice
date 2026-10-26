package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.logging.MachineLog
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
}

private sealed class LogTestState : ViewState {
	data object Idle : LogTestState()
	data class Started(val label: String) : LogTestState()
}

private sealed class LogTestAction : ViewAction {
	data class Start(val label: String) : LogTestAction()

	// Not a data class: the log names it by its class instead of printing its address.
	class Reset : LogTestAction()
}

private sealed class LogTestEffect : ViewEffect {
	data object Begun : LogTestEffect()
}
