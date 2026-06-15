package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import com.gdavidpb.tuindice.testkit.mvi.sealedSubclassesOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MachineDefinitionTest {
	private sealed class TestState : ViewState {
		data class Idle(val count: Int = 0) : TestState()
		data class Running(val count: Int = 0) : TestState()
	}

	private sealed class TestEvent {
		data object Increment : TestEvent()
		data object Start : TestEvent()
		data object Stop : TestEvent()
		data object Ping : TestEvent()
	}

	private sealed class TestEffect {
		data object Beep : TestEffect()
		data object Boop : TestEffect()
	}

	@Test
	fun internalTransition_appliesOutput_andStaysInState() = runTest {
		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				on<TestEvent.Increment> { state, _ -> state.copy(count = state.count + 1) }
			}
		}

		val result = machine.process(TestState.Idle(count = 1), TestEvent.Increment)

		val transitioned = assertIs<TransitionResult.Transitioned<TestState>>(result)
		assertEquals(TestState.Idle(count = 2), transitioned.toState)
	}

	@Test
	fun event_withoutDeclaredTransition_isRejected() = runTest {
		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				on<TestEvent.Increment> { state, _ -> state }
			}
		}

		val fromRunning = machine.process(TestState.Running(), TestEvent.Increment)
		val unknownEvent = machine.process(TestState.Idle(), TestEvent.Stop)

		assertIs<TransitionResult.Rejected<TestState>>(fromRunning)
		assertIs<TransitionResult.Rejected<TestState>>(unknownEvent)
	}

	@Test
	fun stateSpecificTransition_winsOverMachineLevelTransition() = runTest {
		val handlers = mutableListOf<String>()

		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				on<TestEvent.Ping> { state, _ ->
					handlers += "idle"
					state
				}
			}

			fromAny {
				on<TestEvent.Ping> { state, _ ->
					handlers += "any"
					state
				}
			}
		}

		machine.process(TestState.Idle(), TestEvent.Ping)
		machine.process(TestState.Running(), TestEvent.Ping)

		assertEquals(listOf("idle", "any"), handlers)
	}

	@Test
	fun stateChangingTransition_runsExitOutputEnter_inOrder() = runTest {
		val calls = mutableListOf<String>()

		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				onTo<TestEvent.Start, TestState.Running> { state, _ ->
					calls += "output"
					TestState.Running(count = state.count)
				}

				onExit { calls += "exit" }
			}

			from<TestState.Running> {
				onEnter { calls += "enter" }

				on<TestEvent.Ping> { state, _ ->
					calls += "internal"
					state
				}
			}
		}

		val result = machine.process(TestState.Idle(count = 7), TestEvent.Start)

		val transitioned = assertIs<TransitionResult.Transitioned<TestState>>(result)
		assertEquals(TestState.Running(count = 7), transitioned.toState)
		assertEquals(listOf("exit", "output", "enter"), calls)

		calls.clear()
		machine.process(TestState.Running(), TestEvent.Ping)

		assertEquals(listOf("internal"), calls, "Internal transitions must not run enter/exit")
	}

	@Test
	fun machineLevelInternalTransition_thatChangesStateClass_fails() = runTest {
		val machine = MachineDefinition.define<TestState> {
			fromAny {
				on<TestEvent.Ping> { _, _ -> TestState.Running() }
			}
		}

		assertFailsWith<IllegalStateException> {
			machine.process(TestState.Idle(), TestEvent.Ping)
		}
	}

	@Test
	fun declaredTarget_isEnforcedAgainstProducedState() = runTest {
		val machine = MachineDefinition<TestState>(
			transitions = listOf(
				TransitionSpec(
					from = TestState.Idle::class,
					on = TestEvent.Start::class,
					to = TestState.Running::class,
					output = { state, _ -> state }
				)
			),
			enterActions = emptyMap(),
			exitActions = emptyMap()
		)

		assertFailsWith<IllegalStateException> {
			machine.process(TestState.Idle(), TestEvent.Start)
		}
	}

	@Test
	fun wildcardTransition_withDeclaredTarget_changesStateFromAnywhere() = runTest {
		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				on<TestEvent.Stop> { state, _ -> state }
			}

			fromAny {
				onTo<TestEvent.Stop, TestState.Running> { _, _ -> TestState.Running() }
			}
		}

		val fromRunning = machine.process(TestState.Running(count = 1), TestEvent.Stop)
		val fromIdle = machine.process(TestState.Idle(), TestEvent.Stop)

		val transitioned = assertIs<TransitionResult.Transitioned<TestState>>(fromRunning)
		assertEquals(TestState.Running(), transitioned.toState)

		val specific = assertIs<TransitionResult.Transitioned<TestState>>(fromIdle)
		assertEquals(TestState.Idle(), specific.toState, "State-specific row must win over wildcard")

		val diagram = machine.exportToMermaid(
			machineName = "test_machine",
			initialState = TestState.Idle::class
		)

		assertTrue(
			diagram.contains("test_machine --> running : Stop"),
			"Expected wildcard targeted transition in export:\n$diagram"
		)
	}

	@Test
	fun alphabetValidator_detectsUncoveredInputs() = runTest {
		// Enforcement requires sealed-hierarchy reflection (android host run).
		if (sealedSubclassesOf(TestEvent::class) == null) return@runTest

		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				on<TestEvent.Increment> { state, _ -> state }
			}
		}

		assertFailsWith<AssertionError> {
			assertMachineCoversAlphabet(machine, TestEvent::class)
		}

		assertMachineCoversAlphabet(
			machine,
			TestEvent::class,
			except = setOf(
				TestEvent.Start::class,
				TestEvent.Stop::class,
				TestEvent.Ping::class
			)
		)
	}

	@Test
	fun duplicateRows_failFastAtBuildTime() = runTest {
		assertFailsWith<IllegalArgumentException> {
			MachineDefinition.define<TestState> {
				from<TestState.Idle> {
					on<TestEvent.Ping> { state, _ -> state }
					on<TestEvent.Ping> { state, _ -> state }
				}
			}
		}

		assertFailsWith<IllegalArgumentException> {
			MachineDefinition.define<TestState> {
				from<TestState.Idle> {
					onExit { }
				}

				from<TestState.Idle> {
					onExit { }
				}
			}
		}
	}

	@Test
	fun declaredEmits_appearInExport_andEffectsValidatorDetectsDeadSymbols() = runTest {
		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				on<TestEvent.Ping>(emits = setOf(TestEffect.Beep::class)) { state, _ -> state }
			}
		}

		val diagram = machine.exportToMermaid(
			machineName = "test_machine",
			initialState = TestState.Idle::class
		)

		assertTrue(
			diagram.contains("idle --> idle : Ping / Beep"),
			"Expected σ / λ edge label in export:\n$diagram"
		)

		// Enforcement requires sealed-hierarchy reflection (android host run).
		if (sealedSubclassesOf(TestEffect::class) == null) return@runTest

		assertFailsWith<AssertionError> {
			assertMachineCoversEffects(machine, TestEffect::class)
		}

		assertMachineCoversEffects(
			machine,
			TestEffect::class,
			except = setOf(TestEffect.Boop::class)
		)
	}

	@Test
	fun export_containsStatesAndDeclaredTransitions() = runTest {
		val machine = MachineDefinition.define<TestState> {
			from<TestState.Idle> {
				onTo<TestEvent.Start, TestState.Running> { state, _ -> TestState.Running(count = state.count) }
			}

			from<TestState.Running> {
				onTo<TestEvent.Stop, TestState.Idle> { state, _ -> TestState.Idle(count = state.count) }
			}

			fromAny {
				on<TestEvent.Ping> { state, _ -> state }
			}
		}

		val diagram = machine.exportToMermaid(
			machineName = "test_machine",
			initialState = TestState.Idle::class
		)

		val expectedFragments = listOf(
			"stateDiagram-v2",
			"%% machine: test_machine",
			"state idle",
			"state running",
			"state \"any state\" as test_machine",
			"[*] --> idle",
			"idle --> running : Start",
			"running --> idle : Stop",
			"test_machine --> test_machine : Ping"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to contain '$fragment':\n$diagram"
			)
		}

		// Regression guard: the diagram must stay flat. A `state X { … X --> … }`
		// composite wrapper makes Mermaid reject the self-parent cycle on the
		// machine-level (fromAny) rows ("Setting X as parent of X would create a cycle").
		assertFalse(
			diagram.contains("state test_machine {"),
			"Mermaid export must not wrap states in a composite (self-parent cycle):\n$diagram"
		)
	}
}
