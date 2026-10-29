package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryInternalEvent
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder

// The outcomes of a load. Each one has its own row and its own target, so the table says which state a load leads to.
internal fun MachineDefinitionBuilder<AssistantMemory.State>.assistantMemoryLoadingTransitions() {
	from<AssistantMemory.State.Loading> {
		onTo<AssistantMemoryInternalEvent.EntriesLoaded, AssistantMemory.State.Content> { _, event ->
			AssistantMemory.State.Content(entries = event.entries)
		}

		onTo<AssistantMemoryInternalEvent.NothingLoaded, AssistantMemory.State.Empty> { _, _ ->
			AssistantMemory.State.Empty
		}

		onTo<AssistantMemoryInternalEvent.LoadFailed, AssistantMemory.State.Failed> { _, event ->
			AssistantMemory.State.Failed(message = event.message)
		}
	}
}
