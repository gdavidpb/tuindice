package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Machine host that records every capability call instead of routing it to a ViewModel:
 * effects and internal events are captured for assertions, and machine jobs run in the
 * provided scope (use runTest's backgroundScope so they are cancelled with the test).
 */
class RecordingMachineHost<E : ViewEffect>(
	private val scope: CoroutineScope
) : MachineHost<E> {
	val sentEffects = mutableListOf<E>()
	val producedInternalEvents = mutableListOf<Any>()

	override fun sendEffect(effect: E) {
		sentEffects += effect
	}

	override fun processInternalEvent(event: Any) {
		producedInternalEvents += event
	}

	override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
		return scope.launch { block() }
	}
}

/** Outcome of [assertMachineRandomWalk], for extra assertions or debugging. */
class MachineWalkReport(
	val steps: Int,
	val accepted: Int,
	val rejected: Int,
	val visitedRows: Int,
	val totalRows: Int,
	val finalStateName: String?
) {
	val rowCoverage: Double
		get() = if (totalRows == 0) 1.0 else visitedRows.toDouble() / totalRows
}

/**
 * Model-based smoke over the transition table: drives [screenMachine] with [steps] events
 * drawn from [sampleEvents] by a seeded — hence reproducible — random walk.
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
	scope: CoroutineScope,
	steps: Int = 400,
	seed: Long = 0x7E57AB1E,
	feedProducedInternalEvents: Boolean = true,
	minRowCoverage: Double = 0.0
): MachineWalkReport {
	require(sampleEvents.isNotEmpty()) { "sampleEvents must not be empty" }

	val host = RecordingMachineHost<E>(scope = scope)
	val machine = screenMachine.define(host = host)
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

	val report = MachineWalkReport(
		steps = steps,
		accepted = accepted,
		rejected = rejected,
		visitedRows = visitedRows.size,
		totalRows = machine.table.size,
		finalStateName = state::class.simpleName
	)

	assertTrue(
		report.rowCoverage >= minRowCoverage,
		"Random walk (seed=$seed) visited ${report.visitedRows}/${report.totalRows} rows " +
			"(${report.rowCoverage}), below the required $minRowCoverage — extend sampleEvents " +
			"or steps, or lower the expectation deliberately"
	)

	return report
}
