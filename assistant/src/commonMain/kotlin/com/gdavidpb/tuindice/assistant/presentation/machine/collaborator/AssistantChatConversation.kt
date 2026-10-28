package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

/**
 * The conversation the sheet shows: the observation of the store. The observation is started once per sheet, and is
 * stopped with the content.
 */
class AssistantChatConversation(
	private val observeAssistantConversationUseCase: ObserveAssistantConversationUseCase
) {
	private var conversationJob: Job? = null

	// Started once per sheet: a second observer would feed the same machine twice.
	fun observe(host: MachineHost<AssistantChat.Effect>) {
		if (conversationJob?.isActive == true) return

		conversationJob = host.launchMachineJob {
			observeAssistantConversationUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AssistantChatInternalEvent.ConversationObserved(conversation = useCaseState.value)
					)
				}
			}
		}
	}

	fun stop() {
		conversationJob?.cancel()
	}
}
