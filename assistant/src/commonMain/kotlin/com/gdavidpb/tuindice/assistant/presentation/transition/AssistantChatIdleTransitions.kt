package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<AssistantChat.State>.assistantChatIdleTransitions(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	from<AssistantChat.State.Idle> {
		on<AssistantChat.Action.Open> { state, action ->
			machine.openSheet(host = host, context = action.context)
			state
		}

		onTo<AssistantChatInternalEvent.OpeningUnavailable, AssistantChat.State.Unavailable>(
			emits = setOf(AssistantChat.Effect.Dismiss::class)
		) { _, _ ->
			host.sendEffect(AssistantChat.Effect.Dismiss)
			AssistantChat.State.Unavailable
		}

		onTo<AssistantChatInternalEvent.OpeningNeedsConsent, AssistantChat.State.ConsentRequired> { _, _ ->
			AssistantChat.State.ConsentRequired
		}

		onTo<AssistantChatInternalEvent.OpeningReady, AssistantChat.State.Content> { _, _ ->
			machine.enterContent(host = host)
		}
	}
}
