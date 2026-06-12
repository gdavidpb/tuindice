package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

/**
 * A screen's machine: the declarative core of the formal 6-tuple — S₀ ([initialState]),
 * T and G (the table built by [define]) — plus dependencies and commands. Injected into
 * [StateMachineViewModel] as an overridden property — the same pattern use cases follow
 * for their collaborators (e.g. exception handlers).
 */
interface ScreenMachine<S : ViewState, E : ViewEffect> {
	fun initialState(): S

	fun define(host: MachineHost<E>): MachineDefinition<S>
}
