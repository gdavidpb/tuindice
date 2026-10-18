package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The rows valid in any state: the availability observed while the sheet is open, and the dismissal.
internal fun MachineDefinitionBuilder<AssistantChat.State>.assistantChatAnyStateTransitions(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	fromAny {
		on<AssistantChatInternalEvent.AssistantOn> { state, event ->
			machine.session.modeSaid(guided = event.guided)
			state
		}

		// The assistant went off while the sheet was open: from any state, the sheet closes.
		onTo<AssistantChatInternalEvent.AssistantOff, AssistantChat.State.Unavailable>(
			emits = setOf(AssistantChat.Effect.Dismiss::class)
		) { _, _ ->
			host.sendEffect(AssistantChat.Effect.Dismiss)
			AssistantChat.State.Unavailable
		}

		on<AssistantChat.Action.Dismiss>(emits = setOf(AssistantChat.Effect.Dismiss::class)) { state, _ ->
			host.sendEffect(AssistantChat.Effect.Dismiss)
			state
		}
	}
}
