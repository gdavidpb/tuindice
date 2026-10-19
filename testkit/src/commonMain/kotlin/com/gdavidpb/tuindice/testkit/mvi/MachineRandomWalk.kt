package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionSpec
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Model-based smoke over the transition table: drives [screenMachine] with [steps] events
 * drawn from [sampleEvents] by a seeded — hence reproducible — random walk.
 *
 * The coverage floor [minRowCoverage] is measured over the rows of the table minus [unreachableRows], each of which
 * must name a row of the table. A visit to an excluded row is not counted.
 *
 * On every accepted step it asserts the row contract the engine enforces at runtime:
 * the produced state is an instance of the declared target (internal rows must keep the
 * state class), and every effect the row emitted is declared in its `emits` set — the
 * dynamic side of Λ coverage that the static validators cannot check. Enter/exit actions
 * are deliberately not exercised: the walk validates the table, not the commands.
 *
 * Internal events produced by machine jobs (async command results built from the test's
 * fakes) are appended to the candidate pool when [feedProducedInternalEvents] is set, so
 * async-result rows get visited with realistic payloads.
 */
suspend fun <S : ViewState, E : ViewEffect> assertMachineRandomWalk(
	screenMachine: ScreenMachine<S, E>,
	sampleEvents: List<Any>,
	coroutineScope: CoroutineScope,
	steps: Int = 400,
	seed: Long = 0x7E57AB1E,
	feedProducedInternalEvents: Boolean = true,
	unreachableRows: List<UnreachableRow> = emptyList(),
	minRowCoverage: Double = 0.0
): MachineWalkReport {
	require(sampleEvents.isNotEmpty()) { "sampleEvents must not be empty" }

	val host = RecordingMachineHost<E>(coroutineScope = coroutineScope)
	val machine = screenMachine.define(host = host)
	fun TransitionSpec<S>.isNamedBy(unreachable: UnreachableRow) = from == unreachable.from && on == unreachable.on

	val unnamed = unreachableRows.filterNot { unreachable ->
		machine.table.any { spec -> spec.isNamedBy(unreachable) }
	}
	require(unnamed.isEmpty()) {
		"unreachableRows must name rows of the table, not: " +
			unnamed.map { unreachable -> "${unreachable.from?.simpleName ?: "any"}/${unreachable.on.simpleName}" }
	}
	val excludedRows = machine.table.indices.filter { row ->
		unreachableRows.any { unreachable -> machine.table[row].isNamedBy(unreachable) }
	}
	val random = Random(seed)
	val pool = sampleEvents.toMutableList()
	val visitedRows = mutableSetOf<Int>()
	var state: S = screenMachine.initialState()
	var accepted = 0
	var rejected = 0
	var pooledInternalEvents = 0

	repeat(steps) { step ->
		if (feedProducedInternalEvents) {
			while (pooledInternalEvents < host.producedInternalEvents.size) {
				pool += host.producedInternalEvents[pooledInternalEvents]
				pooledInternalEvents++
			}
		}

		val event = pool[random.nextInt(pool.size)]
		val spec = machine.resolve(state = state, event = event)

		if (spec == null) {
			rejected++
			return@repeat
		}

		val effectsBefore = host.sentEffects.size
		val next = try {
			spec.output(state, event)
		} catch (e: Throwable) {
			fail(
				"Random walk (seed=$seed) step $step: ${event::class.simpleName} " +
					"in ${state::class.simpleName} threw ${e::class.simpleName}: ${e.message}",
				e
			)
		}

		val declaredTarget = spec.to
		if (declaredTarget != null) {
			assertTrue(
				declaredTarget.isInstance(next),
				"Random walk (seed=$seed) step $step: ${event::class.simpleName} in " +
					"${state::class.simpleName} declared target ${declaredTarget.simpleName} " +
					"but produced ${next::class.simpleName}"
			)
		} else {
			assertTrue(
				next::class == state::class,
				"Random walk (seed=$seed) step $step: internal row on " +
					"${event::class.simpleName} must stay in ${state::class.simpleName} " +
					"but produced ${next::class.simpleName}"
			)
		}

		val emitted = host.sentEffects.drop(effectsBefore)
		val undeclared = emitted.filter { effect ->
			spec.emits.none { declared -> declared.isInstance(effect) }
		}
		assertTrue(
			undeclared.isEmpty(),
			"Random walk (seed=$seed) step $step: ${event::class.simpleName} in " +
				"${state::class.simpleName} emitted effects outside its declared emits set: " +
				"${undeclared.map { it::class.simpleName }}"
		)

		visitedRows += machine.table.indexOf(spec)
		accepted++
		state = next
	}

	val reachableRows = machine.table.indices.filterNot { row -> row in excludedRows }
	val visitedReachable = reachableRows.filter { row -> row in visitedRows }
	val report = MachineWalkReport(
		steps = steps,
		accepted = accepted,
		rejected = rejected,
		visitedRows = visitedReachable.size,
		totalRows = reachableRows.size,
		finalStateName = state::class.simpleName
	)

	val unvisited = reachableRows.filterNot { row -> row in visitedRows }.map { row ->
		val spec = machine.table[row]
		"${spec.from?.simpleName ?: "any"}/${spec.on.simpleName}"
	}
	assertTrue(
		report.rowCoverage >= minRowCoverage,
		"Random walk (seed=$seed) visited ${report.visitedRows}/${report.totalRows} reachable rows " +
			"(${report.rowCoverage}; ${excludedRows.size} unreachable rows excluded), below the required $minRowCoverage; " +
			"not visited: $unvisited — extend sampleEvents or steps, or lower the expectation deliberately"
	)

	return report
}
