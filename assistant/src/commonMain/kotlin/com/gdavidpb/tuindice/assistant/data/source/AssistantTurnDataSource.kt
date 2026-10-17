package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import kotlinx.coroutines.flow.Flow

// The turns span two origins: the REST calls of the API, and the event stream of the turn.
class AssistantTurnDataSource(
	private val api: KtorAssistantApiDataSource,
	private val eventsRepository: AssistantEventsDataRepository
) : AssistantTurnRepository {
	override suspend fun sendTurn(request: TurnRequest): TurnAccepted = api.postTurn(request)

	override suspend fun cancelTurn(turnId: String) = api.postTurnCancel(turnId)

	override fun turnEvents(turnId: String, afterSeq: Long): Flow<AssistantEvent> {
		return eventsRepository.events(turnId, afterSeq)
	}

	override suspend fun deleteConversations() = api.deleteThreads()
}
