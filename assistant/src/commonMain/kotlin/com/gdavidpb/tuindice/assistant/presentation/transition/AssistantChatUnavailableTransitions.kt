package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder

internal fun MachineDefinitionBuilder<AssistantChat.State>.assistantChatUnavailableTransitions() {
	from<AssistantChat.State.Unavailable> {
		// The sheet is already closing: the assistant going off again changes nothing and dismisses nothing twice.
		on<AssistantChatInternalEvent.AssistantOff> { state, _ -> state }
	}
}
