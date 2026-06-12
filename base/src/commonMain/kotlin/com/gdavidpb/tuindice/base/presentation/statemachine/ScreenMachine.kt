package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

/**
 * A screen's machine: dependencies, commands, and the table definition. Injected into
 * [StateMachineViewModel] as an overridden property — the same pattern use cases follow
 * for their collaborators (e.g. exception handlers).
 */
interface ScreenMachine<S : ViewState, E : ViewEffect> {
	fun define(host: MachineHost<E>): MachineDefinition<S>
}
