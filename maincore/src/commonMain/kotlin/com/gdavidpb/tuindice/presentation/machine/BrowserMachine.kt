package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.transition.browserTransitions

/**
 * A pure machine: every input is synchronous, so there are no use cases, no commands
 * and no internal events — the table is the whole behavior.
 */
class BrowserMachine : ScreenMachine<Browser.State, Browser.Effect> {
	override fun initialState(): Browser.State = Browser.State.Idle

	override fun define(host: MachineHost<Browser.Effect>): MachineDefinition<Browser.State> {
		return MachineDefinition.define {
			browserTransitions(host = host)
		}
	}
}
