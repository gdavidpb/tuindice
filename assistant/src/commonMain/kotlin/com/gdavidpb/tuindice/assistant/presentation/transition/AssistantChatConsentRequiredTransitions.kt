package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The consent the student gives before the chat opens. A refused acceptance keeps the sheet here, with its error, and
// the student can accept again. The privacy policy opens over the sheet and leaves the consent as it was.
internal fun MachineDefinitionBuilder<AssistantChat.State>.assistantChatConsentRequiredTransitions(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	from<AssistantChat.State.ConsentRequired> {
		consentRows(machine, host)
	}

	from<AssistantChat.State.ConsentFailed> {
		consentRows(machine, host)
	}
}

private fun <S : AssistantChat.State> MachineDefinitionBuilder.StateTransitions<AssistantChat.State, S>.consentRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.AcceptConsent> { state, _ ->
		machine.access.acceptConsent(host = host)
		state
	}

	on<AssistantChat.Action.DeclineConsent>(emits = setOf(AssistantChat.Effect.Dismiss::class)) { state, _ ->
		host.sendEffect(AssistantChat.Effect.Dismiss)
		state
	}

	on<AssistantChat.Action.OpenPrivacyPolicy>(
		emits = setOf(AssistantChat.Effect.NavigateToBrowser::class)
	) { state, _ ->
		machine.access.openPrivacyPolicy(host = host, title = machine.texts.privacyPolicyTitle())
		state
	}

	onTo<AssistantChatInternalEvent.ConsentAccepted, AssistantChat.State.Content> { _, _ ->
		machine.enterContent(host = host)
	}

	onTo<AssistantChatInternalEvent.ConsentAcceptFailed, AssistantChat.State.ConsentFailed> { _, _ ->
		AssistantChat.State.ConsentFailed
	}
}
