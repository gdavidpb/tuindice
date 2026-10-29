package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// A failed load: Retry asks for the list again.
internal fun MachineDefinitionBuilder<AssistantMemory.State>.assistantMemoryFailedTransitions(
	machine: AssistantMemoryMachine,
	host: MachineHost<AssistantMemory.Effect>
) {
	from<AssistantMemory.State.Failed> {
		onTo<AssistantMemory.Action.Retry, AssistantMemory.State.Loading> { _, _ ->
			machine.load(host = host)
			AssistantMemory.State.Loading
		}
	}
}
