package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The screen before its first load. The view model sends Load as its initial action, once the screen starts.
internal fun MachineDefinitionBuilder<AssistantMemory.State>.assistantMemoryIdleTransitions(
	machine: AssistantMemoryMachine,
	host: MachineHost<AssistantMemory.Effect>
) {
	from<AssistantMemory.State.Idle> {
		onTo<AssistantMemory.Action.Load, AssistantMemory.State.Loading> { _, _ ->
			machine.load(host = host)
			AssistantMemory.State.Loading
		}
	}
}
