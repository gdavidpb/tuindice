package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/** Follows the events of a turn into the conversation, and holds the assistant off when the server disables it. */
class AssistantTurnStream(
	private val turnRepository: AssistantTurnRepository,
	private val conversationRepository: AssistantConversationRepository,
	private val serverDisabledRepository: AssistantServerDisabledRepository
) {
	fun follow(turnId: String, afterSeq: Long): Flow<AssistantEvent> {
		return turnRepository.turnEvents(turnId = turnId, afterSeq = afterSeq).onEach { event ->
			conversationRepository.applyEvent(event)

			if (event is RunError && event.code == RunErrorCode.ASSISTANT_DISABLED) {
				serverDisabledRepository.markServerDisabled(event.retryAfterSeconds)
			}
		}
	}
}
