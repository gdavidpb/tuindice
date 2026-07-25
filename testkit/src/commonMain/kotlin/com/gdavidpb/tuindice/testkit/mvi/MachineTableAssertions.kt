package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionSpec
import kotlin.reflect.KClass
import kotlin.test.assertTrue

/**
 * Asserts that every direct sealed subclass of each [alphabetRoots] entry has at least
 * one row in the machine's transition table, except those explicitly listed in [except]
 * (inputs that are deliberately invalid in every state).
 *
 * On platforms without sealed-hierarchy reflection this check cannot run: it reports a
 * visible SKIPPED line and returns, so a green run never implies alphabet coverage here.
 * The android host run (testAndroidHostTest, aggregated by verifySharedHostTests) is the
 * enforcing platform.
 */
fun <S : ViewState> assertMachineCoversAlphabet(
	machine: MachineDefinition<S>,
	vararg alphabetRoots: KClass<*>,
	except: Set<KClass<*>> = emptySet()
) {
	val alphabet = mutableListOf<KClass<*>>()

	for (root in alphabetRoots) {
		val subclasses = sealedSubclassesOf(root)
			?: return reportCoverageSkipped(check = "assertMachineCoversAlphabet")
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
 * Asserts that every direct sealed subclass of [effectsRoot] is declared in some row's
 * `emits` set, except those explicitly listed in [except] — i.e. the output alphabet Λ
 * has no dead symbols. On platforms without sealed-hierarchy reflection this check cannot
 * run: it reports a visible SKIPPED line and returns. The android host run
 * (testAndroidHostTest, aggregated by verifySharedHostTests) is the enforcing platform.
 */
fun <S : ViewState> assertMachineCoversEffects(
	machine: MachineDefinition<S>,
	effectsRoot: KClass<*>,
	except: Set<KClass<*>> = emptySet()
) {
	val effects = sealedSubclassesOf(effectsRoot)
		?: return reportCoverageSkipped(check = "assertMachineCoversEffects")
	val declared = machine.table.flatMap { spec -> spec.emits }.toSet()
	val missing = effects.filter { effect ->
		effect !in except && effect !in declared
	}

	assertTrue(
		missing.isEmpty(),
		"Effects never declared by any transition: ${missing.map { it.simpleName }}"
	)
}

/**
 * Asserts that no row shadows a later one.
 *
 * `build()` rejects duplicates by class *equality*, but `resolve()` matches by
 * *subtyping* and takes the first hit, so a row declared on a sealed parent silently
 * swallows every later row declared on its children — consistent for the validator,
 * broken for the runtime.
 *
 * Only the event dimension is checked, and only between rows with the same `from`
 * (or both machine-level). A row whose `from` is a state *supertype* of another's is
 * not detected: that would need the state hierarchy too, and the DSL makes it far
 * harder to write by accident.
 *
 * On platforms without sealed-hierarchy reflection this check cannot run: it reports a
 * visible SKIPPED line and returns. The android host run is the enforcing platform.
 */
fun <S : ViewState> assertMachineHasNoShadowedRows(
	machine: MachineDefinition<S>,
	vararg alphabetRoots: KClass<*>
) {
	val descendants = mutableMapOf<KClass<*>, MutableSet<KClass<*>>>()

	for (root in alphabetRoots) {
		if (!collectDescendants(root, descendants)) {
			return reportCoverageSkipped(check = "assertMachineHasNoShadowedRows")
		}
	}

	val rows = machine.table
	val shadowed = rows.flatMapIndexed { index: Int, earlier ->
		rows.drop(index + 1)
			.filter { later -> earlier.shadows(later, descendants) }
			.map { later ->
				"${earlier.from?.simpleName ?: "*"} × ${earlier.on.simpleName} " +
					"shadows ${later.from?.simpleName ?: "*"} × ${later.on.simpleName}"
			}
	}

	assertTrue(
		shadowed.isEmpty(),
		"Rows declared on a sealed parent swallow later rows on its children " +
			"(resolve matches by subtyping and takes the first hit): $shadowed"
	)
}

private fun <S : ViewState> TransitionSpec<S>.shadows(
	later: TransitionSpec<S>,
	descendants: Map<KClass<*>, Set<KClass<*>>>
): Boolean {
	if (from != later.from) return false

	return later.on != on && later.on in descendants[on].orEmpty()
}

private fun collectDescendants(
	root: KClass<*>,
	into: MutableMap<KClass<*>, MutableSet<KClass<*>>>
): Boolean {
	if (root in into) return true

	val direct = sealedSubclassesOf(root)
	val all = into.getOrPut(root) { mutableSetOf() }

	direct?.forEach { child ->
		if (collectDescendants(child, into)) {
			all += child
			all += into[child].orEmpty()
		}
	}

	return direct != null
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

private fun reportCoverageSkipped(check: String) {
	println(
		"[testkit] $check SKIPPED on this platform (no sealed-hierarchy reflection) — " +
			"enforced by testAndroidHostTest; run ./gradlew verifySharedHostTests"
	)
}
