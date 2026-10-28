package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * A row declared unreachable that the walk reaches anyway has a stale exclusion: the floor is measured without it, so
 * the walk names it in its report and in the message of the floor, and warns about it.
 */
class MachineRandomWalkTest {
	private sealed interface Lamp : ViewState {
		data object Off : Lamp
		data object On : Lamp
	}

	private data object Toggle
	private data object Tick

	private val lamp = object : ScreenMachine<Lamp, ViewEffect> {
		override fun initialState(): Lamp = Lamp.Off

		override fun define(host: MachineHost<ViewEffect>): MachineDefinition<Lamp> =
			MachineDefinition.define<Lamp> {
				from<Lamp.Off> {
					onTo<Toggle, Lamp.On> { _, _ -> Lamp.On }
					on<Tick> { state, _ -> state }
				}
				from<Lamp.On> {
					onTo<Toggle, Lamp.Off> { _, _ -> Lamp.Off }
				}
			}
	}

	@Test
	fun aRowDeclaredUnreachableThatTheWalkVisitsIsNamedInTheReport() = runTest {
		val report = assertMachineRandomWalk(
			screenMachine = lamp,
			sampleEvents = listOf(Toggle, Tick),
			coroutineScope = backgroundScope,
			steps = 200,
			unreachableRows = listOf(UnreachableRow(from = Lamp.Off::class, on = Tick::class, reason = "stale")),
		)

		assertEquals(listOf("Off/Tick"), report.visitedUnreachableRows)
	}

	@Test
	fun aRowDeclaredUnreachableThatTheWalkNeverDrawsIsNotNamed() = runTest {
		val report = assertMachineRandomWalk(
			screenMachine = lamp,
			sampleEvents = listOf(Toggle),
			coroutineScope = backgroundScope,
			steps = 200,
			unreachableRows = listOf(UnreachableRow(from = Lamp.Off::class, on = Tick::class, reason = "never drawn")),
		)

		assertTrue(report.visitedUnreachableRows.isEmpty(), "named without a visit: ${report.visitedUnreachableRows}")
	}

	@Test
	fun theMessageOfTheFloorNamesTheUnreachableRowsTheWalkVisited() = runTest {
		val failure = assertFailsWith<AssertionError> {
			assertMachineRandomWalk(
				screenMachine = lamp,
				sampleEvents = listOf(Toggle, Tick),
				coroutineScope = backgroundScope,
				steps = 200,
				unreachableRows = listOf(UnreachableRow(from = Lamp.Off::class, on = Tick::class, reason = "stale")),
				minRowCoverage = 1.5,
			)
		}

		assertTrue(failure.message.orEmpty().contains("Off/Tick"), "the floor's message does not name it: ${failure.message}")
	}
}
