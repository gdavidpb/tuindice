package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewState
import kotlin.reflect.KClass

/**
 * One row of the formal transition table: from a state class, on an event class,
 * to a declared target state class, applying [output] as the transition function f
 * and emitting at most the effects declared in [emits] (G as a may-emit set).
 *
 * - [from] null means the transition is valid from any state (machine-level).
 * - [to] null means an internal transition: the state class must not change.
 * - [emits] is the declared output alphabet of the row: emitting an undeclared
 *   effect — or emitting outside any transition — fails loudly at runtime.
 */
class TransitionSpec<S : ViewState>(
	val from: KClass<out S>?,
	val on: KClass<*>,
	val to: KClass<out S>?,
	val emits: Set<KClass<*>> = emptySet(),
	val output: suspend (S, Any) -> S
)
