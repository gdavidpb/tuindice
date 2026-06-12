package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import kotlin.reflect.KClass
import kotlin.test.assertTrue

/**
 * Asserts that every direct sealed subclass of each [alphabetRoots] entry has at least
 * one row in the machine's transition table, except those explicitly listed in [except]
 * (inputs that are deliberately invalid in every state).
 *
 * On platforms without sealed-hierarchy reflection this is a no-op; the android host
 * test run is the enforcing platform.
 */
fun <S : ViewState> assertMachineCoversAlphabet(
	machine: MachineDefinition<S>,
	vararg alphabetRoots: KClass<*>,
	except: Set<KClass<*>> = emptySet()
) {
	val alphabet = mutableListOf<KClass<*>>()

	for (root in alphabetRoots) {
		val subclasses = sealedSubclassesOf(root) ?: return
		alphabet += subclasses
	}

	val coveredEvents = machine.table.map { spec -> spec.on }.toSet()
	val missing = alphabet.filter { input ->
		input !in except && input !in coveredEvents
	}

	assertTrue(
		missing.isEmpty(),
		"Inputs without any declared transition: ${missing.map { it.simpleName }}"
	)
}

/**
 * Asserts that every state class declared in the transition table is reachable from
 * [initialState] following declared state-changing transitions. Pure table math, runs
 * on every platform.
 */
fun <S : ViewState> assertMachineStatesReachable(
	machine: MachineDefinition<S>,
	initialState: KClass<out S>
) {
	val declared = machine.table
		.flatMap { spec -> listOfNotNull(spec.from, spec.to) }
		.toSet() + initialState

	val edges = machine.table.mapNotNull { spec ->
		val from = spec.from ?: return@mapNotNull null
		val to = spec.to ?: return@mapNotNull null
		from to to
	}

	val reachable = mutableSetOf<KClass<out S>>(initialState)

	// A machine-level row (from == null) is an edge out of every state, so its target
	// is reachable as soon as any state is — and the initial state always is.
	machine.table.forEach { spec ->
		val to = spec.to
		if (spec.from == null && to != null) reachable += to
	}
	var grew = true
	while (grew) {
		grew = false
		for ((from, to) in edges) {
			if (from in reachable && to !in reachable) {
				reachable += to
				grew = true
			}
		}
	}

	val unreachable = declared - reachable

	assertTrue(
		unreachable.isEmpty(),
		"States declared in the table but unreachable from " +
			"${initialState.simpleName}: ${unreachable.map { it.simpleName }}"
	)
}
