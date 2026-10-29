package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantEvaluationCardsUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.GetAssistantEvaluationCardsParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.mapper.listedEvaluationIds
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.chatcontract.message.Component
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last

/**
 * The conversation the sheet shows: the observation of the store, with the cards of the Evaluations tab for the
 * evaluations its lists name. The observation is started once per sheet, and is stopped with the content.
 */
class AssistantChatConversation(
	private val observeAssistantConversationUseCase: ObserveAssistantConversationUseCase,
	private val getAssistantEvaluationCardsUseCase: GetAssistantEvaluationCardsUseCase
) {
	private var conversationJob: Job? = null

	// The cards read last, and the evaluations they were read for. A turn being written is observed many times while the
	// lists it names stay the same, so the cards are read again only when those evaluations change, or the sheet opens.
	private var cardsReadFor: Set<String>? = null
	private var cards: Map<String, EvaluationCardItem> = emptyMap()

	// Started once per sheet: a second observer would feed the same machine twice.
	fun observe(host: MachineHost<AssistantChat.Effect>) {
		if (conversationJob?.isActive == true) return

		cardsReadFor = null

		conversationJob = host.launchMachineJob {
			observeAssistantConversationUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AssistantChatInternalEvent.ConversationObserved(
							conversation = useCaseState.value,
							evaluationCards = evaluationCardsOf(useCaseState.value)
						)
					)
				}
			}
		}
	}

	fun stop() {
		conversationJob?.cancel()
	}

	// The cards for the evaluations the lists of [conversation] name. A read that fails leaves the lists drawn from what
	// the chat carries, as for an evaluation the device does not have.
	private suspend fun evaluationCardsOf(conversation: AssistantConversation): Map<String, EvaluationCardItem> {
		val evaluationIds = conversation.items
			.filterIsInstance<AssistantConversationItem.Agent>()
			.flatMap { item -> item.message.parts }
			.filterIsInstance<Component>()
			.map { part -> part.component }
			.listedEvaluationIds()

		if (evaluationIds == cardsReadFor) return cards

		val read = if (evaluationIds.isEmpty()) {
			emptyMap()
		} else {
			val useCaseState = getAssistantEvaluationCardsUseCase
				.execute(GetAssistantEvaluationCardsParams(evaluationIds = evaluationIds))
				.last()

			(useCaseState as? UseCaseState.Data)?.value.orEmpty()
		}

		cardsReadFor = evaluationIds
		cards = read

		return read
	}
}
